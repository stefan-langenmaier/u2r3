package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class CaxEqc2LitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(CaxEqc2LitRule.class);
	
	CaxEqc2LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionLit;
		
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		relationManager.getRelation(RelationName.equivalentClass).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (literal, class, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT clsA.literal, ec.colLeft, ");
			sql.append(" MIN(clsA.id) AS sourceId1, '" + RelationName.classAssertionLit + "' AS sourceTable1, ");
			sql.append(" MIN(ec.id) AS sourceId2, '" + RelationName.equivalentClass + "' AS sourceTable2");
		} else {
			sql.append(" (literal, class)");
			sql.append("\n\t SELECT DISTINCT clsA.literal, ec.colLeft");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertionLit") + " AS clsA");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("equivalentClass") + " AS ec ON clsA.class = ec.colRight");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT literal");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.literal = clsA.literal AND bottom.class = ec.colLeft");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.literal, ec.colLeft");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionLitity(X,C1) :- classAssertionLitity(X, C2), equivalentClass(C1, C2)";
	}

}
