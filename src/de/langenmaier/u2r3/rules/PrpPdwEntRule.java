package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class PrpPdwEntRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpPdwEntRule.class);
	
	PrpPdwEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.propertyDisjointWith).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
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
		sql.append("\nFROM " + delta.getDeltaName("propertyDisjointWith") + " AS pdw");
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp1 ON pdw.left = prp1.property");
			sql.append("\n\t INNER JOIN objectPropertyAssertion AS prp2 ON pdw.right = prp2.property");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN objectPropertyAssertion AS prp1 ON pdw.left = prp1.property");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp2 ON pdw.right = prp2.property");
		}
		sql.append("\n WHERE prp1.subject = prp2.subject AND prp1.object = prp2.object");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- propertyDisjointWith(P1, P2), objectPropertyAssertion(X, P1, Y), objectPropertyAssertion(X, P2, Y)";
	}

}
