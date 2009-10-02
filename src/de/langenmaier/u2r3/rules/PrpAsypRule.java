package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class PrpAsypRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpAsypRule.class);
	
	PrpAsypRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		String sql = null;
		try {
			sql = buildQuery(delta, aux, false, 0);
			logger.debug("Checking consistency: " + sql);
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				reasonProcessor.setInconsistent(this);
			}
			
			sql = buildQuery(delta, aux, false, 1);
			logger.debug("Checking consistency: " + sql);
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				reasonProcessor.setInconsistent(this);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		String sql = null;
		try {
			sql = buildQuery(delta, newDelta, false, 0);
			logger.debug("Checking consistency: " + sql);
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				reasonProcessor.setInconsistent(this);
			}
			
			sql = buildQuery(delta, newDelta, false, 1);
			logger.debug("Checking consistency: " + sql);
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				reasonProcessor.setInconsistent(this);
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
	 	
		sql.append("SELECT '1' AS res");
		sql.append("\nFROM " + delta.getDeltaName("classAssertion") + " AS clsA");
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp1 ON clsA.class = prp1.property");
			sql.append("\n\t INNER JOIN propertyAssertion AS prp2 ON clsA.class = prp2.property");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN propertyAssertion AS prp1 ON clsA.class = prp1.property");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp2 ON clsA.class = prp2.property");
		}
		sql.append("\n\t\t AND prp1.subject = prp2.object AND prp1.object = prp2.subject");
		sql.append("\nWHERE clsA.type = '" + OWLRDFVocabulary.OWL_ASYMMETRIC_PROPERTY + "'");
		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertion(P, asymmetric), propertyAssertion(X, P, Y), propertyAssertion(Y, P, X)";
	}

}
