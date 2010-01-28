package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqTransLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqTransLitRule.class);
	
	EqTransLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsLit;
		
		relationManager.getRelation(RelationName.sameAsLit).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		return applyCollectiveTwice(delta, aux);
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		return applyImmediateTwice(delta, newDelta);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, colRight, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT sa1.colLeft, sa2.colRight, MIN(sa1.id) AS sourceId1, '" + RelationName.sameAsLit + "' AS sourceTable1, MIN(sa2.id) AS sourceId2, '" + RelationName.sameAsLit + "' AS sourceTable2");
		} else {
			sql.append(" (colLeft, colRight)");
			sql.append("\n\t SELECT DISTINCT sa1.colLeft, sa2.colRight ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("sameAsLit") + " AS sa1 ");
			sql.append("\n\t\t INNER JOIN sameAsLit AS sa2 ON sa1.colRight = sa2.colLeft");
		} else if (run == 1) {
			sql.append("\n\t FROM sameAsLit AS sa1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("sameAsLit") + " AS sa2 ON sa1.colRight = sa2.colLeft");
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT colLeft, colRight");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.colLeft = sa1.colLeft AND bottom.colRight = sa2.colRight) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sa1.colLeft, sa2.colRight");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsLit(A,C) :- sameAsLit(A,B), sameAsLit(B,C)";
	}

}
