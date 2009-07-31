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
	private double weight = 0;

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

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RuleAction) {
			RuleAction ra = (RuleAction) obj;
			if (delta.equals(ra.delta) && rule.equals(ra.rule)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		//XXX koennte vllt noch besser gehen
		//Annahme das Rule Objekte nur einmalig exisitieren und daher immer den selben HashCode haben
		return rule.hashCode();
	}

}
