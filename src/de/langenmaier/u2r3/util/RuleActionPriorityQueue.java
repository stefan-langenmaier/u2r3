package de.langenmaier.u2r3.util;

import java.util.PriorityQueue;

import de.langenmaier.u2r3.util.RuleAction;

public class RuleActionPriorityQueue extends PriorityQueue<RuleAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1535370082574957435L;
	
	
	@Override
	/**
	 * With this change the add-method is not longer logarithmic in its time complexity
	 */
	public boolean add(RuleAction e) {
		if (contains(e)) {
			remove(e);
		}
		return super.add(e);
	}

}
