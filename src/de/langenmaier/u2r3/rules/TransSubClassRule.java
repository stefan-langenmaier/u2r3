package de.langenmaier.u2r3.rules;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;

public class TransSubClassRule extends Rule {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	
	@Override
	public void apply(DeltaRelation delta) {
		logger.trace("Applying Rule (" + toString()  + ") on DeltaRelation: " + delta.toString());
		long rows = 0;
		
		DeltaRelation newDelta = new DeltaRelation(RelationManager.getRelation(RelationName.subClass));
		newDelta.getDelta(); //INSERT target
		
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			//There are no deltas yet		
			try {
				statement = conn.prepareStatement("INSERT INTO subClass_d" + newDelta.getDelta() + " (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM subClass AS t1 INNER JOIN subClass AS t2 " +
						" WHERE t1.super = t2.sub  " +
						")");
				rows = statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				statement = conn.prepareStatement("INSERT INTO subClass_d" + newDelta.getDelta() + " (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM subClass_d"+ delta.getDelta() + " AS t1 INNER JOIN subClass AS t2 " +
						" WHERE t1.super = t2.sub  " +
						")");
				rows = statement.executeUpdate();

				statement = conn.prepareStatement("INSERT INTO subClass_d" + newDelta.getDelta() + " (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM subClass AS t1 INNER JOIN subClass_d"+ delta.getDelta() + " AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	EXCEPT " +
						" SELECT sub, super " +
						" FROM subClass_d" + newDelta.getDelta() + " " +
						")");
				rows += statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * If there is new data then a new reason should be created
		 */
		if (rows > 0) {
			logger.debug("Applying Rule (" + toString()  + ") created data");
			//add new data
			try {
				statement = conn.prepareStatement("INSERT INTO subClass (sub, super) SELECT sub,  super FROM ( " +
						" SELECT sub, super " +
						" FROM subClass_d"+ newDelta.getDelta() + " " +
						" EXCEPT" +
						" SELECT sub, super " +
						" FROM subClass " +
						")");
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//fire reason
			ReasonProcessor rp = ReasonProcessor.getReasonProcessor();
			Reason r = new Reason(RelationManager.getRelation(RelationName.subClass), newDelta);
			rp.add(r);
		} else {
		/**
		 * if there is no new data the delta can be immediately removed.
		 */	
			newDelta.getRelation().dropDelta(newDelta.getDelta());
		}
		logger.trace("Applied Rule (" + toString() + ") on DeltaRelation: " + delta.toString());
	}

	@Override
	public String toString() {
		return "subClass(A,C) :- subClass(A,B), subClass(B,C)";
	}

}
