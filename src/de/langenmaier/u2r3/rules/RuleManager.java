package de.langenmaier.u2r3.rules;

import java.util.HashMap;

import de.langenmaier.u2r3.core.U2R3Reasoner;
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
		scm_cls_equivalentClass, prp_irp, dt_type1, cls_int_1};
	//private static boolean isInitialized = false;
	private HashMap<RuleName, Rule> rules = new HashMap<RuleName, Rule>();
	private U2R3Reasoner reasoner;
	
	public RuleManager(U2R3Reasoner reasoner) {
		this.reasoner = reasoner;
		//initialize(reasoner);
	}
	public Rule getRule(RuleName name) {
		/*if (! isInitialized) {
			initialize();
		}*/
		if (rules.containsKey(name)) {
			return rules.get(name);
		} else {
			throw new U2R3NotImplementedException();
		}
	}


	public void initialize() {
		//if (! isInitialized) {
			rules.put(RuleName.eq_trans, new EqTransRule(reasoner));
			rules.put(RuleName.eq_ref, new EqRefRule(reasoner));
			rules.put(RuleName.eq_sym, new EqSymRule(reasoner));
			rules.put(RuleName.prp_dom_object, new PrpDomObjectRule(reasoner));
			rules.put(RuleName.prp_dom_data, new PrpDomDataRule(reasoner));
			rules.put(RuleName.prp_rng_object, new PrpRngObjectRule(reasoner));
			rules.put(RuleName.prp_rng_data, new PrpRngDataRule(reasoner));
			rules.put(RuleName.cls_nothing_2, new ClsNothing2Rule(reasoner));
			rules.put(RuleName.cls_int_2, new ClsInt2Rule(reasoner));
			rules.put(RuleName.cax_sco, new CaxScoRule(reasoner));
			rules.put(RuleName.scm_cls_subClass, new ScmClsSubClassRule(reasoner));
			rules.put(RuleName.scm_cls_thing, new ScmClsThingRule(reasoner));
			rules.put(RuleName.scm_cls_nothing, new ScmClsNothingRule(reasoner));
			rules.put(RuleName.scm_cls_equivalentClass, new ScmClsEquivalentClassRule(reasoner));
			rules.put(RuleName.prp_irp, new PrpIrpRule(reasoner));
			rules.put(RuleName.dt_type1, new DtType1Rule(reasoner));
			rules.put(RuleName.cls_int_1, new ClsInt1Rule(reasoner));
			
			
		//}
		//isInitialized = true;
	};
	
}
