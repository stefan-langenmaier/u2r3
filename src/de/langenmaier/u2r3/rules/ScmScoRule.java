package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmScoRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmScoRule.class);
	
	ScmScoRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		
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
			sql.append("\n\t SELECT sc1.sub, sc2.super,");
			sql.append(" MIN(sc1.id) AS sourceId1, '" + RelationName.subClass + "' AS sourceTable1,");
			sql.append(" MIN(sc2.id) AS sourceId2, '" + RelationName.subClass + "' AS sourceTable2");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT sc1.sub, sc2.super ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("subClass") + " AS sc1 ");
			sql.append("\n\t\t INNER JOIN subClass AS sc2 ON sc1.super = sc2.sub");
		} else if (run == 1) {
			sql.append("\n\t FROM subClass AS sc1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subClass") + " AS sc2 ON sc1.super = sc2.sub");
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = sc1.sub AND bottom.super = sc2.super) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sc1.sub, sc2.super");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(A,C) :- subClass(A,B), subClass(B,C)";
	}

}
