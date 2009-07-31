package de.langenmaier.u2r3;

import de.langenmaier.u2r3.rules.Rule;

/**
 * This container keeps the information that are necessary to apply a rule.
 * @author stefan
 *
 */
public class RuleAction {
	private Rule rule = null;
	private DeltaRelation delta = null;

	public RuleAction(Rule r) {
		rule = r;
		delta = null;
	}

	public RuleAction(Rule r, DeltaRelation delta) {
		rule = r;
		this.delta = delta;
	}

	public void apply() {
		rule.apply(delta);
	}

}
