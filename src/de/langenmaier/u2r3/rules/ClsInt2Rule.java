package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsInt2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsInt2Rule.class);
	
	ClsInt2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT clsA.class, l.element, MIN(clsA.id) AS classSourceId, '" + RelationName.classAssertion.toString() + "' AS classSourceTable, MIN(l.id) AS typeSourceId, '" + RelationName.list.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT clsA.class, l.element");
		}
		
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM  intersectionOf AS int INNER JOIN list AS l");
			sql.append("\n\t\t ON int.list = l.name");
			sql.append("\n\t\t INNER JOIN classAssertion AS clsA");
			sql.append("\n\t\t ON clsA.type = int.class");
		} else {
			if (delta.getRelation() == relationManager.getRelation(RelationName.intersectionOf)) {
				sql.append("\n\t FROM  " + delta.getDeltaName() + " AS int INNER JOIN list AS l");
				sql.append("\n\t\t ON int.list = l.name");
				sql.append("\n\t\t INNER JOIN classAssertion AS clsA");
				sql.append("\n\t\t ON clsA.type = int.class");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.list)) {
				sql.append("\n\t FROM  intersectionOf AS int INNER JOIN " + delta.getDeltaName() + " AS l");
				sql.append("\n\t\t ON int.list = l.name");
				sql.append("\n\t\t INNER JOIN classAssertion AS clsA");
				sql.append("\n\t\t ON clsA.type = int.class");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.classAssertion)) {
				sql.append("\n\t FROM  intersectionOf AS int INNER JOIN list AS l");
				sql.append("\n\t\t ON int.list = l.name");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS clsA");
				sql.append("\n\t\t ON clsA.type = int.class");
			}
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT class, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = clsA.class AND bottom.type = l.element");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.class, l.element");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(Y,C1..Cn) :- intersectionOf(C, X), list(x, C1..Cn), classAssertion(Y, C)";
	}

}
