package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;

/**
 * This class is just a layer to get the difference between consistency rules
 * and application rules.
 * @author stefan
 *
 */
public abstract class ConsistencyRule extends Rule {
	
	protected ConsistencyRule(U2R3Reasoner reasoner) {
		super(reasoner);
	}

	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		String sql = null;
		try {
			sql = buildQuery(delta, aux, false, 0);
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet						
				logger.trace("Adding delta data (NO_DELTA): " + sql);
			} else {
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
			}
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
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet						
				logger.trace("Adding delta data (NO_DELTA): " + sql);
			} else {
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
			}
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				reasonProcessor.setInconsistent(this);
			}
				
		} catch (SQLException e) {
				e.printStackTrace();
			}
		return rows;
	}

}
