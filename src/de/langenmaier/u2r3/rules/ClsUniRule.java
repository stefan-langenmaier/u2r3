package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsUniRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsUniRule.class);
	
	ClsUniRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.unionOf).addAdditionRule(this);
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
			sql.append("\n\t SELECT clsA.class AS class, uo.class AS type, MIN(clsA.id) AS classSourceId, '" + RelationName.classAssertion + "' AS classSourceTable, MIN(uo.id) AS typeSourceId, '" + RelationName.unionOf + "' AS typeSourceTable");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT clsA.class AS class, uo.class AS type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("unionOf") + " AS uo");
		sql.append("\n\t\t INNER JOIN list AS l ON l.name = uo.list");
		sql.append("\n\t\t INNER JOIN (");
		sql.append("\n\t\t\t SELECT name, COUNT(name) AS anzahl");
		sql.append("\n\t\t\t FROM  list");
		sql.append("\n\t\t\t GROUP BY name");
		sql.append("\n\t\t ) AS anzl ON l.name = anzl.name");
		sql.append("\n\t\t INNER JOIN classAssertion AS clsA ON l.element = clsA.type");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = clsA.class AND bottom.type = uo.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY clsA.class, uo.class");
		sql.append("\n\t HAVING COUNT(*) = anzl.anzahl");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(Y,C) :- unionOf(C, X), list(X, C1..Cn), classAssertion(Y, C1..Cn)";
	}

}
