package de.langenmaier.u2r3.rules;

import java.util.HashMap;

import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

/**
 * This class is a kind of a factory that manages the creation of rules.
 * @author stefan
 *
 */
public class RuleManager {
	public enum RuleName {transSubClass};
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


	private static void initialize() {
		rules.put(RuleName.transSubClass, new TransSubClassRule());
		
		isInitialized = true;
	};
	
}
