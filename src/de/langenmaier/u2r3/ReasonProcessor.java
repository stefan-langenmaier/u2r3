package de.langenmaier.u2r3;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.db.SubClassRelation;
import de.langenmaier.u2r3.rules.Rule;

public class ReasonProcessor {
	private static ReasonProcessor rp = null;
	
	static Logger logger = Logger.getLogger(ReasonProcessor.class);
	
	List<RuleAction> actions = new LinkedList<RuleAction>();
	
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
		do {
			/*for (RuleAction action : actions) {
				action.apply();
			}*/
			RuleAction action;
			while (!(actions.isEmpty())) {
				action = actions.get(0);
				action.apply();
				actions.remove(0);
			}
			
		} while (applyUpdates()); //applyAuxRelations();
		actions = null;
	}

	private boolean applyUpdates() {
		if (SubClassRelation.getRelation().isDirty()) {
			rp.add(new Reason(SubClassRelation.getRelation(), new DeltaRelation()));
			SubClassRelation.getRelation().merge();
		}
		return !(actions.isEmpty());
	}


}
