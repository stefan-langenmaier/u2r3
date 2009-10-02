package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpIfpRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpIfpRule.class);
	
	PrpIfpRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAs;
		
		//relations on the right side
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
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
			sql.append("\n\t SELECT prp1.subject AS left, prp2.subject AS right, MIN(prp1.id) AS leftSourceId, '" + RelationName.propertyAssertion + "' AS leftSourceTable, MIN(prp2.id) AS rightSourceId, '" + RelationName.propertyAssertion + "' AS rightSourceTable");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT prp1.subject AS left, prp2.subject AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertion") + " AS clsA");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp1 ON clsA.class = prp1.property");
			sql.append("\n\t\t INNER JOIN propertyAssertion AS prp2 ON clsA.class = prp2.property AND prp1.object = prp2.object");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN propertyAssertion AS prp1 ON clsA.class = prp1.property");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp2 ON clsA.class = prp2.property AND prp1.object = prp2.object");
		}
		sql.append("\n\t WHERE clsA.type = '" + OWLRDFVocabulary.OWL_INVERSE_FUNCTIONAL_PROPERTY + "'");

		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.left");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = prp1.object AND bottom.right = prp2.object");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp1.subject, prp2.subject");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAs(X1, X2) :- classAssertion(P, 'inverse_functional'), propertyAssertion(X1, P, Y), propertyAssertion(X2, P, Y)";
	}

}
