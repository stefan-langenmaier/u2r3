package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpTrpRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpTrpRule.class);
	
	PrpTrpRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.objectPropertyAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
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
			sql.append(" (subject, property, object, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3)");
			sql.append("\n\t SELECT prp1.subject AS subject, prp2.property AS property, prp2.object AS object, ");
			sql.append(" MIN(clsA.id) AS sourceId1, '" + RelationName.classAssertionEnt + "' AS sourceTable1, ");
			sql.append(" MIN(prp1.id) AS sourceId2, '" + RelationName.objectPropertyAssertion + "' AS sourceTable2, ");
			sql.append(" MIN(prp2.id) AS sourceId3, '" + RelationName.objectPropertyAssertion + "' AS sourceTable3");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT prp1.subject AS subject, prp2.property AS property, prp2.object AS object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp1 ON clsA.entity = prp1.property");
			sql.append("\n\t\t INNER JOIN objectPropertyAssertion AS prp2 ON clsA.entity = prp2.property");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN objectPropertyAssertion AS prp1 ON clsA.entity = prp1.property");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp2 ON clsA.entity = prp2.property");
		}
		sql.append("\n\t\t\t  AND prp1.object = prp2.subject");
		sql.append("\n\t WHERE clsA.class = '" + OWLRDFVocabulary.OWL_TRANSITIVE_PROPERTY + "'");

		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = prp1.subject AND bottom.property = prp1.property AND bottom.object = prp2.object");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp1.subject, prp1.property, prp2.object");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsEnt(Y1, Y2) :- classAssertionEnt(P, 'functional'), objectPropertyAssertion(X, P, Y1), objectPropertyAssertion(X, P, Y2)";
	}

}
