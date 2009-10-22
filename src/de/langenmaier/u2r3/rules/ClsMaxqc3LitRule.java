package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

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
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.onClass).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	/**
	 * Query muss zweimal laufen
	 */
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		String sql = null;
		try {
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet		
				sql = buildQuery(delta, aux, true, 0);
				logger.trace("Adding delta data (NO_DELTA): " + sql);
				rows = statement.executeUpdate(sql);

			} else {
				sql = buildQuery(delta, aux, true, 0);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
				rows = statement.executeUpdate(sql);

				sql = buildQuery(delta, aux, true, 1);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 1): " + sql);
				rows = statement.executeUpdate(sql);
	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	/**
	 * Query muss zweimal laufen
	 */
	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		String sql = null;
		try {
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet		
				sql = buildQuery(delta, newDelta, false, 0);
				logger.trace("Adding delta data (NO_DELTA): " + sql);
				rows = statement.executeUpdate(sql);
	
			} else {
				sql = buildQuery(delta, newDelta, false, 0);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
				rows = statement.executeUpdate(sql);
				
				sql = buildQuery(delta, newDelta, true, 1);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 1): " + sql);
				rows = statement.executeUpdate(sql);
		
			}
		} catch (SQLException e) {
				e.printStackTrace();
			}
		return rows;
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
	
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4, sourceId5, sourceTable5, sourceId6, sourceTable6, sourceId7, sourceTable7, sourceId8, sourceTable8)");
			sql.append("\n\t SELECT prp1.object AS left, prp2.object AS right, ");
			sql.append(" MIN(mqc.id) AS sourceId1, '" + RelationName.maxQualifiedCardinality + "' AS sourceTable1, ");
			sql.append(" MIN(op.id) AS sourceId2, '" + RelationName.onProperty + "' AS sourceTable2, ");
			sql.append(" MIN(oc.id) AS sourceId3, '" + RelationName.onClass + "' AS sourceTable3, ");
			sql.append(" MIN(ca1.id) AS sourceId4, '" + RelationName.classAssertionEnt + "' AS sourceTable4, ");
			sql.append(" MIN(prp1.id) AS sourceId5, '" + RelationName.dataPropertyAssertion + "' AS sourceTable5, ");
			sql.append(" MIN(ca2.id) AS sourceId6, '" + RelationName.classAssertionLit + "' AS sourceTable6, ");
			sql.append(" MIN(prp2.id) AS sourceId7, '" + RelationName.dataPropertyAssertion + "' AS sourceTable7, ");
			sql.append(" MIN(ca3.id) AS sourceId8, '" + RelationName.classAssertionLit + "' AS sourceTable8");
		} else {
			sql.append(" (left, right)");
			sql.append("\n\t SELECT DISTINCT prp1.object AS left, prp2.object AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("maxQualifiedCardinality") + " AS mqc");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON op.class = mqc.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onClass") + " AS oc ON oc.name = mqc.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca1 ON ca1.class = op.class");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp1 ON ca1.class = prp1.subject AND op.property = prp1.property");
			sql.append("\n\t\t INNER JOIN classAssertionLit AS ca2 ON ca2.literal = prp1.object AND ca2.class = oc.class");
			sql.append("\n\t\t INNER JOIN dataPropertyAssertion AS prp2 ON ca1.class = prp2.subject AND op.property = prp2.property");
			sql.append("\n\t\t INNER JOIN classAssertionlit AS ca3 ON ca3.literal = prp1.object AND ca3.class = oc.class");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN dataPropertyAssertion AS prp1 ON ca1.class = prp1.subject AND op.property = prp1.property");
			sql.append("\n\t\t INNER JOIN classAssertionLit AS ca2 ON ca2.literal = prp1.object AND ca2.class = oc.class");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectropertyAssertion") + " AS prp2 ON ca1.class = prp2.subject AND op.property = prp2.property");
			sql.append("\n\t\t INNER JOIN classAssertionLit AS ca3 ON ca3.literal = prp1.object AND ca3.class = oc.class");
			}
		sql.append("\n\t WHERE mqc.value = '1' ");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t\t SELECT bottom.left");
			sql.append("\n\t\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t\t WHERE bottom.left = prp1.object AND bottom.right = prp2.object");
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
