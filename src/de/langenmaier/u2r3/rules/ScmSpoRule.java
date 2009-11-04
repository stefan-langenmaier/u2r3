package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmSpoRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmSpoRule.class);
	
	ScmSpoRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subProperty;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		
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
			sql.append(" (sub, super, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT sp1.sub, sp2.super,");
			sql.append(" MIN(sp1.id) AS sourceId1, '" + RelationName.subProperty + "' AS sourceTable1,");
			sql.append(" MIN(sp2.id) AS sourceId2, '" + RelationName.subProperty + "' AS sourceTable2");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT sp1.sub, sp2.super ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("subProperty") + " AS sp1 ");
			sql.append("\n\t\t INNER JOIN subProperty AS sp2 ON sp1.super = sp2.sub");
		} else if (run == 1) {
			sql.append("\n\t FROM subProperty AS sp1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp2 ON sp1.super = sp2.sub");
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = sp1.sub AND bottom.super = sp2.super) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sp1.sub, sp2.super");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subProperty(A,C) :- subProperty(A,B), subProperty(B,C)";
	}

}
