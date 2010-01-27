package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsMaxc2EntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsMaxc2EntRule.class);
	
	ClsMaxc2EntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsEnt;
		
		relationManager.getRelation(RelationName.maxCardinality).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
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
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
	
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, right, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4)");
			sql.append("\n\t SELECT prp1.object AS colLeft, prp2.object AS right, ");
			sql.append(" MIN(mc.id) AS sourceId1, '" + RelationName.maxCardinality + "' AS sourceTable1, ");
			sql.append(" MIN(ca.id) AS sourceId2, '" + RelationName.classAssertionEnt + "' AS sourceTable2, ");
			sql.append(" MIN(prp1.id) AS sourceId3, '" + RelationName.objectPropertyAssertion + "' AS sourceTable3, ");
			sql.append(" MIN(prp2.id) AS sourceId4, '" + RelationName.objectPropertyAssertion + "' AS sourceTable4");
		} else {
			sql.append(" (colLeft, right)");
			sql.append("\n\t SELECT DISTINCT prp1.object AS colLeft, prp2.object AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("maxCardinality") + " AS mc");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca ON ca.class = mc.class");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp1 ON ca.entity = prp1.subject AND mc.property = prp1.property");
			sql.append("\n\t\t INNER JOIN objectPropertyAssertion AS prp2 ON ca.entity = prp2.subject AND mc.property = prp2.property");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN objectPropertyAssertion AS prp1 ON ca.entity = prp1.subject AND mc.property = prp1.property");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp2 ON ca.entity = prp2.subject AND mc.property = prp2.property");
		}
		sql.append("\n\t WHERE mc.value = '1' ");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t\t SELECT bottom.colLeft");
			sql.append("\n\t\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t\t WHERE bottom.colLeft = prp1.object AND bottom.right = prp2.object");
			sql.append("\n\t\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp1.object, prp2.object");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsEnt(Y1, Y2) :- maxCardinality(X, 1), onProperty(X, P), classAssertionEnt(U, X), objectPropertyAssertion(U, P, Y1), objectPropertyAssertion(U, P, Y2)";
	}

}
