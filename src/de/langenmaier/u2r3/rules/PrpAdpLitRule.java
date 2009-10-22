package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class PrpAdpLitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpAdpLitRule.class);
	
	PrpAdpLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.members).addAdditionRule(this);
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
		sql.append("\nFROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("members") + " AS m ON m.class = clsA.entity");
		sql.append("\n\t INNER JOIN list AS l ON m.list = l.name");
		
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp1 ON l.element = prp1.property");
			sql.append("\n\t INNER JOIN dataPropertyAssertion AS prp2 ON l.element = prp2.property");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN dataPropertyAssertion AS prp1 ON l.element = prp1.property");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp2 ON l.element = prp2.property");
		}
		sql.append("\n WHERE clsA.type = '" + OWLRDFVocabulary.OWL_ALL_DISJOINT_PROPERTIES + "'");
		sql.append("\n\t AND prp1.subject = prp2.subject AND prp1.object = prp2.object AND prp1.property != prp2.propery ");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertion(X, all_disjoint_prop), members(X, Y), list(Y, P1..Pn), dataPropertyAssertion(U, Pi, V), dataPropertyAssertion(U, Pj, V)";
	}

}
