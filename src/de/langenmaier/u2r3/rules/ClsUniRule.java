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
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
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
			sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT clsA.entity, uo.colClass, ");
			sql.append(" MIN(clsA.id) AS sourceId1, '" + RelationName.classAssertionEnt + "' AS sourceTable1, ");
			sql.append(" MIN(uo.id) AS sourceId2, '" + RelationName.unionOf + "' AS sourceTable2");
		} else {
			sql.append(" (entity, colClass)");
			sql.append("\n\t SELECT DISTINCT clsA.entity, uo.colClass");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("unionOf") + " AS uo");
		sql.append("\n\t\t INNER JOIN list AS l ON l.name = uo.list");
		sql.append("\n\t\t INNER JOIN (");
		sql.append("\n\t\t\t SELECT name, COUNT(name) AS anzahl");
		sql.append("\n\t\t\t FROM  list");
		sql.append("\n\t\t\t GROUP BY name");
		sql.append("\n\t\t ) AS anzl ON l.name = anzl.name");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS clsA ON l.element = clsA.colClass");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.entity");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = clsA.entity AND bottom.colClass = uo.colClass");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY clsA.entity, uo.colClass");
		sql.append("\n\t HAVING COUNT(*) = anzl.anzahl");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(Y,C) :- unionOf(C, X), list(X, C1..Cn), classAssertionEnt(Y, C1..Cn)";
	}

}
