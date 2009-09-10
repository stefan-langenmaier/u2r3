package de.langenmaier.u2r3;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.Relation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.RuleActionQueue;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class ReasonProcessor {
	private static ReasonProcessor rp = null;
	
	static Logger logger = Logger.getLogger(ReasonProcessor.class);
	
	RuleActionQueue actions = new RuleActionQueue();
	
	private ReasonProcessor() {}
	
	public synchronized static ReasonProcessor getReasonProcessor() {
		if (rp == null) rp = new ReasonProcessor();
		return rp;
	}
	
	public void add(Reason reason) {
		logger.trace("Processing Reason: " + reason.toString());
		for(Rule r : reason.getRelation().getRules()) {
			//if there is no delta for the reason then its created by the initial import
			if (reason.getDelta() == null) {
				actions.add(new RuleAction(r,
						new DeltaRelation(reason.getRelation(), DeltaRelation.NO_DELTA)));
			} else { // else some new data has created a delta
				actions.add(new RuleAction(r, reason.getDelta()));
			}
		}
		logger.trace("Processed Reason: " + reason.toString());
	}

	public void classify() {
		do {
			RuleAction action;
			while (!(actions.isEmpty())) {
				action = actions.activate();
				action.apply();
				actions.delete(action);
			}
			
		} while (applyUpdates());

	}

	private boolean applyUpdates() {
		if (Settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
			return false;
		} else if (Settings.getDeltaIteration() == DeltaIteration.COLLECTIVE) {
			for (Relation r : RelationManager.getRelations()) {
				if (r.isDirty()) {
					r.merge();
				}
			}
			return (!(actions.isEmpty()));
		}
		return false;
	}

	public String dump() {
		return actions.toString();
	}


}
