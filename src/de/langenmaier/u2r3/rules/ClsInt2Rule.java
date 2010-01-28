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
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
	
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT clsA.entity, l.element, ");
			sql.append(" MIN(clsA.id) AS sourceId1, '" + RelationName.classAssertionEnt + "' AS sourceTable2, ");
			sql.append(" MIN(int.id) AS sourceId2, '" + RelationName.intersectionOf + "' AS sourceTable2");
		} else {
			sql.append(" (entity, colClass)");
			sql.append("\n\t SELECT DISTINCT clsA.entity, l.element");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("intersectionOf") + " AS int INNER JOIN list AS l");
		sql.append("\n\t\t ON int.list = l.name");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t\t ON clsA.colClass = int.colClass");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT entity, colClass");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = clsA.entity AND bottom.colClass = l.element");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.entity, l.element");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(Y,C1..Cn) :- intersectionOf(C, X), list(X, C1..Cn), classAssertionEnt(Y, C)";
	}

}
