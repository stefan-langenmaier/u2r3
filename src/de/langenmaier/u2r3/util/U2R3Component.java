package de.langenmaier.u2r3.util;

import de.langenmaier.u2r3.core.ReasonProcessor;
import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.rules.RuleManager;

public class U2R3Component {
	protected Settings settings;
	protected RelationManager relationManager;
	protected RuleManager ruleManager;
	protected ReasonProcessor reasonProcessor;
	
	protected U2R3Component(U2R3Reasoner reasoner) {
		settings = reasoner.getSettings();
		relationManager = reasoner.getRelationManager();
		ruleManager = reasoner.getRuleManager();
		reasonProcessor = reasoner.getReasonProcessor();
	}
}