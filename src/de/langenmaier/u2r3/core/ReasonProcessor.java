package de.langenmaier.u2r3.core;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.Relation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.rules.ConsistencyRule;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.RuleManager.RuleName;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.RuleAction;
import de.langenmaier.u2r3.util.RuleActionQueue;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class ReasonProcessor {
	static Logger logger = Logger.getLogger(ReasonProcessor.class);
	
	RuleActionQueue actions;
	
	private RuleManager ruleManager;
	private RelationManager relationManager;
	private Settings settings;

	private boolean consistent = true;
	
	ReasonProcessor(U2R3Reasoner reasoner) {
		actions = new RuleActionQueue(reasoner);
		ruleManager = reasoner.getRuleManager();
		relationManager = reasoner.getRelationManager();
		settings = reasoner.getSettings();
		//set the Rule that are always true and need to be run
	}
	
	public void initialize() {
		actions.add(new RuleAction(ruleManager.getRule(RuleName.dt_type1),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA)));
		actions.add(new RuleAction(ruleManager.getRule(RuleName.cls_thing),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA)));
		actions.add(new RuleAction(ruleManager.getRule(RuleName.cls_nothing_1),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA)));
		actions.add(new RuleAction(ruleManager.getRule(RuleName.prp_ap),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA)));

	}
	
	public void add(Reason reason) {
		logger.trace("Processing Reason: " + reason.toString());
		for(Rule r : reason.getRules()) {
			//if there is no delta for the reason then its created by the initial import
			if (reason.getDeltaRelation() == null) {
				actions.add(new RuleAction(r,
						reason.getRelation().createDeltaRelation(DeltaRelation.NO_DELTA)));
			} else { // else some new data has created a delta
				actions.add(new RuleAction(r, reason.getDeltaRelation()));
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
		if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
			return false;
		} else if (settings.getDeltaIteration() == DeltaIteration.COLLECTIVE) {
			//Konsistenzregeln anwenden
			logger.debug("Applying consistency rules");
			for(RuleAction ra : actions.getConsistencyRules()) {
				ra.apply();
			}
			logger.debug("Applied consistency rules");
			logger.trace(" ------------------------------------ ");
			logger.trace(" --------     next round     -------- ");
			logger.trace(" ------------------------------------ ");
			for (Relation r : relationManager.getRelations()) {
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

	public void pause() {
		if (!actions.isEmpty()) {
			throw new U2R3RuntimeException();
		}
		
	}

	public void resume() {
		//classify();		
	}

	public void setInconsistent(ConsistencyRule consistencyRule) {
		consistent = false;
		//System.out.println(consistent);
		//System.out.println("XXXXXXXXXXXXXXXXX" + consistencyRule.toString());
	}
	
	public boolean isConsistent() {
		return consistent;
	}


}
