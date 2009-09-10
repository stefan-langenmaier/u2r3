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
				rows = statement.executeUpdate("INSERT INTO " + aux.getDeltaName() + " (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM " + aux.getTableName() + " AS t1 INNER JOIN " + aux.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"   EXCEPT " + 
						" SELECT sub, super " +
						" FROM " + aux.getDeltaName() + " " +
						")");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rows = statement.executeUpdate("INSERT INTO " + aux.getDeltaName() + " (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM "+ delta.getDeltaName() + " AS t1 INNER JOIN " + aux.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"   EXCEPT " + 
						" SELECT sub, super " +
						" FROM " + aux.getDeltaName() + " " +
						")");

				rows += statement.executeUpdate("INSERT INTO " + aux.getDeltaName() + " (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM " + aux.getTableName() + " AS t1 INNER JOIN "+ delta.getDeltaName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	EXCEPT " +
						" SELECT sub, super " +
						" FROM " + aux.getDeltaName() + " " +
						")");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rows;
	}

	private long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			//There are no deltas yet		
			try {
				rows = statement.executeUpdate("INSERT INTO " + newDelta.getDeltaName() + " (sub, super) SELECT DISTINCT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM " + newDelta.getTableName() + " AS t1 INNER JOIN " + newDelta.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub " +
						")");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rows = statement.executeUpdate("INSERT INTO " + newDelta.getDeltaName() + " (sub, super) SELECT DISTINCT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM "+ delta.getDeltaName() + " AS t1 INNER JOIN " + newDelta.getTableName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						")");

				rows += statement.executeUpdate("INSERT INTO " + newDelta.getDeltaName() + " (sub, super) SELECT DISTINCT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM " + newDelta.getTableName() + " AS t1 INNER JOIN "+ delta.getDeltaName() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	EXCEPT " +
						" SELECT sub, super " +
						" FROM " + newDelta.getDeltaName() + " " +
						")");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rows;
	}

	@Override
	public String toString() {
		return "subClass(A,C) :- subClass(A,B), subClass(B,C)";
	}

}
