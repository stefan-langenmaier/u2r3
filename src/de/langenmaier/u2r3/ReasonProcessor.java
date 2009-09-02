package de.langenmaier.u2r3;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.RuleActionQueue;

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
		for(Rule r : reason.getRelation().getRules()) {
			logger.debug("adding Rule");
			actions.add(new RuleAction(r, reason.getDelta()));
			logger.debug("added Rule");
		}
		
	}

	public void classify() {
		//do {
			/*for (RuleAction action : actions) {
				action.apply();
			}*/
			RuleAction action;
			while (!(actions.isEmpty())) {
				action = actions.activate();
				action.apply();
				actions.delete(action);
			}
			
		//} while (applyUpdates()); //applyAuxRelations();
		//actions = null;
	}

	/*private boolean applyUpdates() {
		if (SubClassRelation.getRelation().isDirty()) {
			
			if (SubClassRelation.getRelation().merge() > 0) {
				rp.add(new Reason(SubClassRelation.getRelation(), new DeltaRelation()));
			}
		}
		return !(actions.isEmpty());
	}*/


}
