package de.langenmaier.u2r3.rules;

import de.langenmaier.u2r3.DeltaRelation;


/**
 * The rule class implements the semantic to create new data from a specific OWL2 RL rule. The rule class itself is abstract, cause there are only specific rules, but all should have a common interface.
 * @author stefan
 *
 */
public abstract class Rule {

	public abstract void apply(DeltaRelation delta);
	
}
