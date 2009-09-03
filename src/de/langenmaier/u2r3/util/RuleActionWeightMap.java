package de.langenmaier.u2r3.util;

import java.util.HashMap;

import de.langenmaier.u2r3.RuleAction;

/**
 * This class stores RuleActions with their last weight.
 * @author stefan
 *
 */
public class RuleActionWeightMap extends HashMap<RuleAction, Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2514695924588018278L;

	public Double put(RuleAction key) {
		if (containsKey(key)) {
			double oldWeight = get(key);
			//here is it possible to tweak the strategy
			key.setWeight(1+oldWeight);
		}
		return super.put(key, key.getWeight());
	}
}
