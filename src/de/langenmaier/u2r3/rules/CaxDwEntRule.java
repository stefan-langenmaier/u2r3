package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class CaxDwEntRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(CaxDwEntRule.class);
	
	CaxDwEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.disjointWith).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
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
		sql.append("\nFROM " + delta.getDeltaName("disjointWith") + " AS dw");
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca1 ON dw.left = ca1.type");
			sql.append("\n\t INNER JOIN classAssertionEnt AS ca2 ON dw.right = ca2.type");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN classAssertionEnt AS ca1 ON dw.left = ca1.type");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca2 ON dw.right = ca2.type");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- disjointWith(C1, C2), classAssertionEnt(X, C1), classAssertionEnt(X, C2)";
	}

}
