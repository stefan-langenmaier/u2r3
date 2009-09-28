package de.langenmaier.u2r3.rules;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.U2R3DBConnection;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.U2R3Component;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;


/**
 * The rule class implements the semantic to create new data from a specific OWL2 RL rule. The rule class itself is abstract, cause there are only specific rules, but all should have a common interface.
 * @author stefan
 *
 */
public abstract class Rule extends U2R3Component{
	static Logger logger = Logger.getLogger(Rule.class);
	
	protected Connection conn = null;
	protected Statement statement = null;
	protected RelationName targetRelation = null;
	
	protected Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		conn = U2R3DBConnection.getConnection();
		
		try {
			statement = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void apply(DeltaRelation delta) {
		logger.trace("Applying Rule (" + toString() + ") on DeltaRelation: " + delta.toString());
		long rows = 0;
		
		DeltaRelation newDelta = null;
		
		if (targetRelation != null) {
			newDelta = relationManager.getRelation(targetRelation).createNewDeltaRelation();
		}
		
		if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
			rows = applyImmediate(delta, newDelta);
		} else if (settings.getDeltaIteration() == DeltaIteration.COLLECTIVE) {
			rows = applyCollective(delta, newDelta);
		} else {
			throw new U2R3RuntimeException();
		}
		
		if (rows > 0) {
			if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
				logger.debug("Applying Rule (" + toString()  + ") created data");
				newDelta.getRelation().merge(newDelta);
			} else if (settings.getDeltaIteration() == DeltaIteration.COLLECTIVE) {
				newDelta.getRelation().makeDirty();
			}
			
		} else {
		/**
		 * if there is no new data the delta can be immediately removed.
		 */	
			if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE && newDelta != null) {
				newDelta.dispose();
			}			
		}
		logger.trace("Applied Rule (" + toString() + ") on DeltaRelation: " + delta.toString());
	}
	
	protected abstract long applyImmediate(DeltaRelation delta, DeltaRelation newDelta);
	protected abstract long applyCollective(DeltaRelation delta, DeltaRelation aux);

	protected abstract String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run);
	
	public abstract String toString();
}
