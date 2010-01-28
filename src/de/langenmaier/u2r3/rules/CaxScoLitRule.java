package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class CaxScoLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(CaxScoLitRule.class);
	
	CaxScoLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionLit;
		
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (literal, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT clsA.literal, sc.super, ");
			sql.append(" MIN(clsA.id) AS sourceId1, '" + RelationName.classAssertionLit + "' AS sourceTable1, ");
			sql.append(" MIN(sc.id) AS sourceId2, '" + RelationName.subClass + "' AS sourceTable2");
		} else {
			sql.append(" (literal, colClass)");
			sql.append("\n\t SELECT DISTINCT clsA.literal, sc.super");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertionLit") + " AS clsA");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subClass") + " AS sc ON clsA.colClass = sc.sub");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT literal");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.literal = clsA.literal AND bottom.colClass = sc.super");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.literal, sc.super");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionLit(X,C2) :- classAssertionLit(X, C1), subClass(C1, C2)";
	}

}
