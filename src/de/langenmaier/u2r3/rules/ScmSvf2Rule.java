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
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
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
			sql.append(" (sub, super, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4, sourceId5, sourceTable5)");
			sql.append("\n\t SELECT op1.class, op2.class,");
			sql.append(" MIN(svf1.id) AS sourceId1, '" + RelationName.someValuesFrom + "' AS sourceTable1,");
			sql.append(" MIN(op1.id) AS sourceId2, '" + RelationName.onProperty + "' AS sourceTable2,");
			sql.append(" MIN(svf2.id) AS sourceId3, '" + RelationName.someValuesFrom + "' AS sourceTable3,");
			sql.append(" MIN(op2.id) AS sourceId4, '" + RelationName.onProperty + "' AS sourceTable4,");
			sql.append(" MIN(sp.id) AS sourceId5, '" + RelationName.subProperty + "' AS sourceTable5");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT op1.class, op2.class ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("someValuesFrom") + " AS svf1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op1 ON op1.class = svf1.part");
			sql.append("\n\t\t INNER JOIN someValuesFrom AS svf2 ON svf1.total = svf2.total");
			sql.append("\n\t\t INNER JOIN onProperty AS op2 ON op2.class = svf2.part");
		} else if (run == 1) {
			sql.append("\n\t FROM someValuesFrom AS svf1 ");
			sql.append("\n\t\t INNER JOIN onProperty AS op1 ON op1.class = svf1.part");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("someValuesFrom") + " AS svf2 ON svf1.total = svf2.total");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op2 ON op2.class = svf2.part");
		}
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.sub = op1.property AND sp.super = op2.property");

		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = op1.class AND bottom.super = op2.class) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY op1.class, op2.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1,C2) :- someValuesFrom(C1, Y), onProperty(C1, P1), someValuesFrom(C2, Y), onProperty(C2, P2), subProperty(P1, P2)";
	}

}
