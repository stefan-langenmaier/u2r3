package de.langenmaier.u2r3.util;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.rules.Rule;

/**
 * This container keeps the information that are necessary to apply a rule.
 * @author stefan
 *
 */
public class RuleAction implements Comparable<RuleAction>{
	private Rule rule = null;
	private DeltaRelation delta = null;
	private double weight = 0;

	/**
	 * Es wird die Regel r mit ausgelöst, dabei ist delta 
	 * die Relation in die geschrieben wird. Es ist nötig das diese
	 * hier angegeben wird.
	 * @param r
	 * @param delta
	 */
	public RuleAction(Rule r, DeltaRelation delta) {
		if (r == null) throw new NullPointerException();
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
		this.weight = new Double(weight);
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
		//This is under the assumption that there exist only unique rules;
		return rule.hashCode() ^ delta.hashCode();
	}

	@Override
	/**
	 * This comparison is used to get an order for the execution of RuleAction.
	 * Here is it possible to change the strategy.
	 */
	public int compareTo(RuleAction o) {
		return (int)(weight-o.weight);
	}

	public DeltaRelation getDeltaRelation() {
		return delta;
	}

	public String toString() {
		return rule.toString() + "(" + weight + ") on " + delta.getTableName() + " (" + delta.getDelta() + ")";
	}
	
	public Rule getRule() {
		return rule;
	}
}
