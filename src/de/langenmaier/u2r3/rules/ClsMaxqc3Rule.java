package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsMaxqc3Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsMaxqc3Rule.class);
	
	ClsMaxqc3Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAs;
		
		relationManager.getRelation(RelationName.maxQualifiedCardinality).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.onClass).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		
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
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT prp1.object AS left, prp2.object AS right, MIN(prp1.id) AS leftSourceId, '" + RelationName.propertyAssertion + "' AS propertySourceTable, MIN(prp2.id) AS rightSourceId, '" + RelationName.propertyAssertion + "' AS rightSourceTable");
		} else {
			sql.append(" (left, right)");
			sql.append("\n\t SELECT DISTINCT prp1.object AS left, prp2.object AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("maxQualifiedCardinality") + " AS mqc");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON op.class = mqc.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onClass") + " AS oc ON oc.name = mqc.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertion") + " AS ca1 ON ca1.type = op.class");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp1 ON ca1.class = prp1.subject AND op.property = prp1.property");
			sql.append("\n\t\t INNER JOIN classAssertion AS ca2 ON ca2.class = prp1.subject AND ca2.type = oc.class");
			sql.append("\n\t\t INNER JOIN propertyAssertion AS prp2 ON ca1.class = prp2.subject AND op.property = prp2.property");
			sql.append("\n\t\t INNER JOIN classAssertion AS ca3 ON ca3.class = prp1.subject AND ca3.type = oc.class");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN propertyAssertion AS prp1 ON ca1.class = prp1.subject AND op.property = prp1.property");
			sql.append("\n\t\t INNER JOIN classAssertion AS ca2 ON ca2.class = prp1.subject AND ca2.type = oc.class");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp2 ON ca1.class = prp2.subject AND op.property = prp2.property");
			sql.append("\n\t\t INNER JOIN classAssertion AS ca3 ON ca3.class = prp1.subject AND ca3.type = oc.class");
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
		return "sameAs(Y1, Y2) :- maxQualifiedCardinality(X, 1), onProperty(X, P), onClass(X, C), classAssertion(U, X), propertyAssertion(U, P, Y1), classAssertion(Y1, C), propertyAssertion(U, P, Y2), classAssertion(Y2, C)";
	}

}
