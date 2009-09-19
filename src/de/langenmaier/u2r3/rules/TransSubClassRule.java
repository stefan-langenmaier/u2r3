package de.langenmaier.u2r3.rules;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class TransSubClassRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	
	TransSubClassRule() {
		RelationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		
		RelationManager.getRelation(RelationName.subClass).addDeletionRule(this);
	}
	
	@Override
	public void apply(DeltaRelation delta) {
		logger.trace("Applying Rule (" + toString() + ") on DeltaRelation: " + delta.toString());
		long rows = 0;
		
		DeltaRelation newDelta = null;

		if (Settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
			newDelta = new DeltaRelation(RelationManager.getRelation(RelationName.subClass));
			rows = applyImmediate(delta, newDelta);
		} else if (Settings.getDeltaIteration() == DeltaIteration.COLLECTIVE) {
			newDelta = delta.getNextDelta();
			rows = applyCollective(delta, newDelta);
		} else {
			throw new U2R3RuntimeException();
		}
		
		if (rows > 0) {
			if (Settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
				logger.debug("Applying Rule (" + toString()  + ") created data");
				newDelta.getRelation().merge(newDelta);
			} else if (Settings.getDeltaIteration() == DeltaIteration.COLLECTIVE) {
				newDelta.getRelation().makeDirty();
			}
			
		} else {
		/**
		 * if there is no new data the delta can be immediately removed.
		 */	
			if (Settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
				newDelta.getRelation().dropDelta(newDelta.getDelta());
			}			
		}
		logger.trace("Applied Rule (" + toString() + ") on DeltaRelation: " + delta.toString());
	}




	
	private long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			//There are no deltas yet		
			try {
				rows = statement.executeUpdate("INSERT INTO " + aux.getDeltaName() + " (sub, super, subSourceId, superSourceId) SELECT sub,  super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId  FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId  " +
						" FROM " + delta.getTableName() + " AS t1 INNER JOIN " + delta.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"   AND NOT EXISTS (SELECT sub, super, subSourceId, superSourceId  FROM " + aux.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) " +
						") GROUP BY sub, super");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rows = statement.executeUpdate("INSERT INTO " + aux.getDeltaName() + " (sub, super, subSourceId, superSourceId) SELECT sub,  super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId  FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId  " +
						" FROM "+ delta.getDeltaName() + " AS t1 INNER JOIN " + aux.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"   AND NOT EXISTS (SELECT sub, super, subSourceId, superSourceId  FROM " + aux.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) " +
						") GROUP BY sub, super");

				rows += statement.executeUpdate("INSERT INTO " + aux.getDeltaName() + " (sub, super, subSourceId, superSourceId) SELECT sub,  super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId  FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId  " +
						" FROM " + aux.getTableName() + " AS t1 INNER JOIN "+ delta.getDeltaName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	AND NOT EXISTS (SELECT sub, super, subSourceId, superSourceId  FROM " + aux.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) " +
						") GROUP BY sub, super");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rows;
	}

	private long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		String sql = null;
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			//There are no deltas yet		
			try {
				sql = "INSERT INTO " + newDelta.getDeltaName() + " (sub, super, subSourceId, superSourceId) SELECT sub, super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId FROM ( " +
					" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId " +
					" FROM " + newDelta.getTableName() + " AS t1 INNER JOIN " + newDelta.getTableName() + " AS t2 " +
					" WHERE t1.super = t2.sub " +
					") GROUP BY sub, super";
				rows = statement.executeUpdate(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rows = statement.executeUpdate("INSERT INTO " + newDelta.getDeltaName() + " (sub, super, subSourceId, superSourceId) SELECT sub, super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId  " +
						" FROM "+ delta.getDeltaName() + " AS t1 INNER JOIN " + newDelta.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						") GROUP BY sub, super");

				rows += statement.executeUpdate("INSERT INTO " + newDelta.getDeltaName() + " (sub, super, subSourceId, superSourceId) SELECT sub, super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId  " +
						" FROM " + newDelta.getTableName() + " AS t1 INNER JOIN "+ delta.getDeltaName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"   AND NOT EXISTS (SELECT sub, super, subSourceId, superSourceId  FROM " + newDelta.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) " +
						") GROUP BY sub, super");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rows;
	}
	
	private String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again) {
		String sql = null;
		String historyFields = ", subSourceId, superSourceId";
		String historyAggregateFields = ", MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId";
		String normalAggregateFields = "GROUP BY sub, super";
		
		sql = "INSERT INTO " + newDelta.getDeltaName() +
				" (sub, super" + historyFields + ")" +
				" SELECT sub, super " + historyAggregateFields +
				" FROM ( " +
					" SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId  " +
					" FROM " + newDelta.getTableName() + " AS t1 INNER JOIN "+ delta.getDeltaName() + " AS t2 " +
					" WHERE t1.super = t2.sub  " +
					"	AND NOT EXISTS (SELECT sub, super, subSourceId, superSourceId  FROM " + newDelta.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) " +
				" ) " + normalAggregateFields;
		return sql;
	}

	@Override
	public String toString() {
		return "subClass(A,C) :- subClass(A,B), subClass(B,C)";
	}

}
