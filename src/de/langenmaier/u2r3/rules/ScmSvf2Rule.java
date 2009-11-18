package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmSvf2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmSvf2Rule.class);
	
	ScmSvf2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.someValuesFrom).addAdditionRule(this);
		
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
			sql.append("\n\t SELECT svf1.part, svf2.part,");
			sql.append(" MIN(svf1.id) AS sourceId1, '" + RelationName.someValuesFrom + "' AS sourceTable1,");
			sql.append(" MIN(svf2.id) AS sourceId2, '" + RelationName.someValuesFrom + "' AS sourceTable2,");
			sql.append(" MIN(sp.id) AS sourceId3, '" + RelationName.subProperty + "' AS sourceTable3");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT svf1.part, svf2.part ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("someValuesFrom") + " AS svf1 ");
			sql.append("\n\t\t INNER JOIN someValuesFrom AS svf2 ON svf1.total = svf2.total");
		} else if (run == 1) {
			sql.append("\n\t FROM someValuesFrom AS svf1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("someValuesFrom") + " AS svf2 ON svf1.total = svf2.total");
		}
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.sub = svf1.property AND sp.super = svf1.property");

		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = svf1.part AND bottom.super = svf2.part) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY svf1.part, svf2.part");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1,C2) :- someValuesFrom(C1, Y), onProperty(C1, P1), someValuesFrom(C2, Y), onProperty(C2, P2), subProperty(P1, P2)";
	}

}
