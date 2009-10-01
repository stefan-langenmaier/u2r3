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
	public enum RuleName {eq_trans, eq_ref, eq_sym, cls_nothing_2,
		cls_int_2, cax_sco, scm_cls_subClass, scm_cls_thing, scm_cls_nothing,
		scm_cls_equivalentClass, prp_irp, dt_type1, cls_int_1, prp_dom, prp_rng,
		eq_rep_s, eq_rep_p, eq_rep_o};

	private HashMap<RuleName, Rule> rules = new HashMap<RuleName, Rule>();
	private U2R3Reasoner reasoner;
	
	public RuleManager(U2R3Reasoner reasoner) {
		this.reasoner = reasoner;

	}
	public Rule getRule(RuleName name) {
		if (rules.containsKey(name)) {
			return rules.get(name);
		} else {
			throw new U2R3NotImplementedException();
		}
	}


	public void initialize() {
		rules.put(RuleName.eq_trans, new EqTransRule(reasoner));
		rules.put(RuleName.eq_ref, new EqRefRule(reasoner));
		rules.put(RuleName.eq_sym, new EqSymRule(reasoner));
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
		rules.put(RuleName.prp_dom, new PrpDomRule(reasoner));
		rules.put(RuleName.prp_rng, new PrpRngRule(reasoner));
		rules.put(RuleName.eq_rep_s, new EqRepSRule(reasoner));
		rules.put(RuleName.eq_rep_p, new EqRepPRule(reasoner));
		rules.put(RuleName.eq_rep_o, new EqRepORule(reasoner));
	};
	
}
