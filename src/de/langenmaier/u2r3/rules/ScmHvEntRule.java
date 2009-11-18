package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmHvEntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmHvEntRule.class);
	
	ScmHvEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.hasValueEnt).addAdditionRule(this);
		
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
			sql.append("\n\t SELECT hv1.class, hv2.class, ");
			sql.append(" MIN(hv1.id) AS sourceId1, '" + RelationName.hasValueEnt + "' AS sourceTable1, ");
			sql.append(" MIN(hv2.id) AS sourceId2, '" + RelationName.hasValueEnt + "' AS sourceTable2, ");
			sql.append(" MIN(sp.id) AS sourceId3, '" + RelationName.subProperty + "' AS sourceTable3");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT hv1.class, hv2.class ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("hasValueEnt") + " AS hv1 ");
			sql.append("\n\t\t INNER JOIN hasValueEnt AS hv2 ON hv1.value = hv2.value");
		} else if (run == 1) {
			sql.append("\n\t FROM hasValueEnt AS hv1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("hasValueEnt") + " AS hv2 ON hv1.value = hv2.value");
		}
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.sub = hv1.property AND sp.super = hv2.property");

		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = hv1.class AND bottom.super = hv2.class) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY hv1.class, hv2.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1,C2) :- hasValueEnt(C1, I), onProperty(C1, P1), hasValueEnt(C2, I), onProperty(C2, P2), subProperty(P1, P2)";
	}

}
