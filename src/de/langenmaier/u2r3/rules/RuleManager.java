package de.langenmaier.u2r3.rules;

import java.util.HashMap;

import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

/**
 * This class is a kind of a factory that manages the creation of rules.
 * @author stefan
 *
 */
public class RuleManager {
	public enum RuleName {eq_trans, eq_ref};
	private static boolean isInitialized = false;
	private static HashMap<RuleName, Rule> rules = new HashMap<RuleName, Rule>();
	
	
	public static Rule getRule(RuleName name) {
		if (! isInitialized) {
			initialize();
		}
		if (rules.containsKey(name)) {
			return rules.get(name);
		} else {
			throw new U2R3NotImplementedException();
		}
	}


	public static void initialize() {
		if (! isInitialized) {
			rules.put(RuleName.eq_trans, new EqTransRule());
			rules.put(RuleName.eq_ref, new EqRefRule());
		}
		isInitialized = true;
	};
	
}
