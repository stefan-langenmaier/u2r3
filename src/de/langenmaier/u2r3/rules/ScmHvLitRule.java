package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmHvLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmHvLitRule.class);
	
	ScmHvLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.hasValueLit).addAdditionRule(this);
		
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
			sql.append(" (sub, super, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3)");
			sql.append("\n\t SELECT hv1.colClass, hv2.colClass, ");
			sql.append(" MIN(hv1.id) AS sourceId1, '" + RelationName.hasValueLit + "' AS sourceTable1, ");
			sql.append(" MIN(hv2.id) AS sourceId2, '" + RelationName.hasValueLit + "' AS sourceTable2, ");
			sql.append(" MIN(sp.id) AS sourceId3, '" + RelationName.subProperty + "' AS sourceTable3");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT hv1.colClass, hv2.colClass ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("hasValueLit") + " AS hv1 ");
			sql.append("\n\t\t INNER JOIN hasValueLit AS hv2 ON hv1.value = hv2.value");
		} else if (run == 1) {
			sql.append("\n\t FROM hasValueLit AS hv1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("hasValueLit") + " AS hv2 ON hv1.value = hv2.value");
		}
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.sub = hv1.property AND sp.super = hv2.property");

		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = hv1.colClass AND bottom.super = hv2.colClass) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY hv1.colClass, hv2.colClass");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1,C2) :- hasValueLit(C1, I), onProperty(C1, P1), hasValueLit(C2, I), onProperty(C2, P2), subProperty(P1, P2)";
	}

}
