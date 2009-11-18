package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmAvf1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmAvf1Rule.class);
	
	ScmAvf1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		relationManager.getRelation(RelationName.allValuesFrom).addAdditionRule(this);
		
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
			sql.append("\n\t SELECT avf1.part, avf2.part,");
			sql.append(" MIN(avf1.id) AS sourceId1, '" + RelationName.allValuesFrom + "' AS sourceTable1,");
			sql.append(" MIN(avf2.id) AS sourceId2, '" + RelationName.allValuesFrom + "' AS sourceTable2,");
			sql.append(" MIN(sc.id) AS sourceId3, '" + RelationName.subClass + "' AS sourceTable3");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT avf1.part, avf2.part ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("allValuesFrom") + " AS avf1 ");
			sql.append("\n\t\t INNER JOIN allValuesFrom AS avf2 ON avf1.property = avf2.property");
		} else if (run == 1) {
			sql.append("\n\t FROM allValuesFrom AS avf1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("allValuesFrom") + " AS avf2 ON avf1.property = avf2.property");
		}
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subClass") + " AS sc ON sc.sub = avf1.total AND sc.super = avf2.total");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = avf1.part AND bottom.super = avf2.part) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY avf1.part, avf2.part");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1,C2) :- allValuesFrom(C1, Y1), onProperty(C1, P), allValuesFrom(C2, Y2), onProperty(C2, P), subClass(Y1, Y2)";
	}

}
