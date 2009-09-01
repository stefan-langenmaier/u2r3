package de.langenmaier.u2r3.util;

import java.util.HashMap;

import de.langenmaier.u2r3.DeltaRelation;
import de.langenmaier.u2r3.RuleAction;
/**
 * This is a helper class for the RuleActionQueue. It stores the number of a certain DeltaRelation that is in the queue.
 * @author stefan
 *
 */

public class RuleActionDeltaMap extends HashMap<DeltaRelation, Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1558592147678134925L;

	public Long put(RuleAction ra) {
		Long old = new Long(1);
		if (containsKey(ra.getDeltaRelation())) {
			old = new Long(get(ra.getDeltaRelation()).longValue() + 1);
		}
		return super.put(ra.getDeltaRelation(), old);
	}

	/**
	 * This method reduces the count for a the delta-Iteration of the RuleAction
	 * If this was the last delta-Iteration of a kind aka the count has dropped to zero
	 * the method return true
	 * @param ra
	 * @return
	 */
	public boolean reduce(RuleAction ra) {
		Long old;
		if (containsKey(ra.getDeltaRelation())) {
			old = get(ra.getDeltaRelation());
			old = new Long(old.longValue()-1);
			remove(ra.getDeltaRelation());
			if (old.longValue()>0) {
				put(ra.getDeltaRelation(), old);
				return false;
			}
		}
		return true;
	}
}
