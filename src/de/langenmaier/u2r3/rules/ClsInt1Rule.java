package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsInt1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsInt1Rule.class);
	
	ClsInt1Rule(U2R3Reasoner reasoner) {
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
			sql.append("\n\t SELECT clsA.class AS class, int.class AS type, MIN(clsA.id) AS classSourceId, '" + RelationName.classAssertion.toString() + "' AS classSourceTable, MIN(int.id) AS typeSourceId, '" + RelationName.intersectionOf.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT clsA.class AS subject, int.class AS type");
		}
		
		sql.append("\n\t FROM (SELECT name, COUNT(name) AS anzahl FROM list GROUP BY name) AS  anzl");
		sql.append("\n\t\t INNER JOIN list AS l ON anzl.name = l.name");
		if (delta.getDelta() == DeltaRelation.NO_DELTA || delta.getRelation() == relationManager.getRelation(RelationName.list)) {	
			sql.append("\n\t\t INNER JOIN classAssertion AS clsA ON l.element = clsA.type");
			sql.append("\n\t\t INNER JOIN intersectionOf AS int ON int.list = l.name");
		} else {
			if (delta.getRelation() == relationManager.getRelation(RelationName.intersectionOf)) {
				sql.append("\n\t\t INNER JOIN classAssertion AS clsA ON l.element = clsA.type");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS int ON int.list = l.name");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.classAssertion)) {
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS clsA ON l.element = clsA.type");
				sql.append("\n\t\t INNER JOIN intersectionOf AS int ON int.list = l.name");
			}
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = clsA.class AND bottom.type = int.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY l.name, clsA.class, type");
		sql.append("\n\t HAVING COUNT(l.name) = anzl.anzahl");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(Y,C) :- intersectionOf(C, X), list(X, C1..Cn), classAssertion(Y, C1..Cn)";
	}

}
