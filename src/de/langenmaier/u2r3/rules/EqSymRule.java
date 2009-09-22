package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqSymRule extends Rule {
	static Logger logger = Logger.getLogger(EqSymRule.class);
	
	EqSymRule() {
		targetRelation = RelationName.sameAs;
		
		//relations on the right side
		RelationManager.getRelation(RelationName.sameAs).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		RelationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
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
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet		
				sql = buildQuery(delta, newDelta, false, 0);
				logger.trace("Adding delta data (NO_DELTA): " + sql);
				rows = statement.executeUpdate(sql);
	
			} else {
				sql = buildQuery(delta, newDelta, false, 0);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
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
/*		SELECT right, left, id AS leftSourceId, 'sameAs' AS leftSourceTabel, id AS rightSourceId, 'sameAs' AS leftSourceTabel
		FROM sameAs AS top
		WHERE NOT EXISTS (SELECT right, left FROM sameAs AS bottom WHERE bottom.left = top.left AND bottom.right = top.right) 
*/
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT right, left, id AS leftSourceId, '" + RelationName.sameAs.toString() + "' AS leftSourceTable, id AS rightSourceId, '" + RelationName.sameAs.toString() + "' AS rightSourceTable");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT right, left");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT right, left");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = top.left AND bottom.right = top.right");
			sql.append("\n\t )");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAs(B,A) :- sameAs(A,B)";
	}

}
