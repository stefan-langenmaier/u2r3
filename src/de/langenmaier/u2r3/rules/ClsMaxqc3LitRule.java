package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsMaxqc3LitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsMaxqc3LitRule.class);
	
	ClsMaxqc3LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsLit;
		
		relationManager.getRelation(RelationName.maxQualifiedCardinality).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		
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
			sql.append(" (colLeft, colRight, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4, sourceId5, sourceTable5, sourceId6, sourceTable6)");
			sql.append("\n\t SELECT prp1.object AS colLeft, prp2.object AS colRight, ");
			sql.append(" MIN(mqc.id) AS sourceId1, '" + RelationName.maxQualifiedCardinality + "' AS sourceTable1, ");
			sql.append(" MIN(ca1.id) AS sourceId2, '" + RelationName.classAssertionEnt + "' AS sourceTable2, ");
			sql.append(" MIN(prp1.id) AS sourceId3, '" + RelationName.dataPropertyAssertion + "' AS sourceTable3, ");
			sql.append(" MIN(ca2.id) AS sourceId4, '" + RelationName.classAssertionLit + "' AS sourceTable4, ");
			sql.append(" MIN(prp2.id) AS sourceId5, '" + RelationName.dataPropertyAssertion + "' AS sourceTable5, ");
			sql.append(" MIN(ca3.id) AS sourceId6, '" + RelationName.classAssertionLit + "' AS sourceTable6");
		} else {
			sql.append(" (colLeft, colRight)");
			sql.append("\n\t SELECT DISTINCT prp1.object AS colLeft, prp2.object AS colRight");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("maxQualifiedCardinality") + " AS mqc");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca1 ON ca1.colClass = mqc.colClass");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp1 ON ca1.colClass = prp1.subject AND mqc.property = prp1.property");
			sql.append("\n\t\t INNER JOIN classAssertionLit AS ca2 ON ca2.literal = prp1.object AND ca2.colClass = mqc.total");
			sql.append("\n\t\t INNER JOIN dataPropertyAssertion AS prp2 ON ca1.colClass = prp2.subject AND mqc.property = prp2.property");
			sql.append("\n\t\t INNER JOIN classAssertionlit AS ca3 ON ca3.literal = prp1.object AND ca3.colClass = mqc.total");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN dataPropertyAssertion AS prp1 ON ca1.colClass = prp1.subject AND mqc.property = prp1.property");
			sql.append("\n\t\t INNER JOIN classAssertionLit AS ca2 ON ca2.literal = prp1.object AND ca2.colClass = mqc.total");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp2 ON ca1.colClass = prp2.subject AND mqc.property = prp2.property");
			sql.append("\n\t\t INNER JOIN classAssertionLit AS ca3 ON ca3.literal = prp1.object AND ca3.colClass = mqc.total");
			}
		sql.append("\n\t WHERE mqc.value = '1' ");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t\t SELECT bottom.colLeft");
			sql.append("\n\t\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t\t WHERE bottom.colLeft = prp1.object AND bottom.colRight = prp2.object");
			sql.append("\n\t\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp1.object, prp2.object");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsLit(Y1, Y2) :- maxQualifiedCardinality(X, 1), onProperty(X, P), onClass(X, C), classAssertionEnt(U, X), dataPropertyAssertion(U, P, Y1), classAssertionLit(Y1, C), dataPropertyAssertion(U, P, Y2), classAssertionLit(Y2, C)";
	}

}
