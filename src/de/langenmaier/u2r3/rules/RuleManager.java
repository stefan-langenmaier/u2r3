package de.langenmaier.u2r3.rules;

import java.util.HashMap;

import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

/**
 * This class is a kind of a factory that manages the creation of rules.
 * @author stefan
 *
 */
public class RuleManager {
	public enum RuleName {eq_trans, eq_ref, eq_sym, prp_dom_object,
		prp_dom_data, prp_rng_object, prp_rng_data, cls_nothing_2,
		cls_int_2, cax_sco, scm_cls_subClass, scm_cls_thing, scm_cls_nothing,
		scm_cls_equivalentClass};
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
			rules.put(RuleName.eq_sym, new EqSymRule());
			rules.put(RuleName.prp_dom_object, new PrpDomObjectRule());
			rules.put(RuleName.prp_dom_data, new PrpDomDataRule());
			rules.put(RuleName.prp_rng_object, new PrpRngObjectRule());
			rules.put(RuleName.prp_rng_data, new PrpRngDataRule());
			rules.put(RuleName.cls_nothing_2, new ClsNothing2Rule());
			rules.put(RuleName.cls_int_2, new ClsInt2Rule());
			rules.put(RuleName.cax_sco, new CaxScoRule());
			rules.put(RuleName.scm_cls_subClass, new ScmClsSubClassRule());
			rules.put(RuleName.scm_cls_thing, new ScmClsThingRule());
			rules.put(RuleName.scm_cls_nothing, new ScmClsNothingRule());
			rules.put(RuleName.scm_cls_equivalentClass, new ScmClsEquivalentClassRule());
			
		}
		isInitialized = true;
	};
	
}
