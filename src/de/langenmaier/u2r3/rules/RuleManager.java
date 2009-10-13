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
		eq_rep_s, eq_rep_p, eq_rep_o, eq_diff_1, eq_diff_2, eq_diff_3,
		prp_fp, prp_ifp, prp_symp, prp_asyp, prp_trp, prp_spo_1, prp_spo_2,
		prp_eqp_1, prp_eqp_2, prp_pdw, prp_adw, prp_inv_1, prp_inv_2,
		prp_key, prp_npa_1, prp_npa_2, cls_thing, cls_nothing_1, cls_uni,
		cls_com, cls_svf_1, cls_svf_2, cls_avf, cls_hv_1, cls_hv_2,
		cls_maxc_1, cls_maxc_2, cls_maxqc_1, cls_maxqc_2, cls_maxqc_3,
		cls_maxqc_4, cls_oo, cax_eqc_1, cax_eqc_2, cax_dw, scm_sco,
		scm_eqc_1, scm_eqc_2, scm_op_sub, scm_op_eq, scm_dp_sub, scm_dp_eq,
		scm_spo, scm_eqp_1, scm_eqp_2, scm_dom_1, scm_dom_2, scm_rng_1,
		scm_rng_2, scm_hv, scm_svf_2, scm_svf_1, scm_avf_1, scm_avf_2,
		scm_int, scm_uni};

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
		rules.put(RuleName.eq_diff_1, new EqDiff1Rule(reasoner));
		rules.put(RuleName.eq_diff_2, new EqDiff2Rule(reasoner));
		rules.put(RuleName.eq_diff_3, new EqDiff3Rule(reasoner));
		rules.put(RuleName.prp_fp, new PrpFpRule(reasoner));
		rules.put(RuleName.prp_ifp, new PrpIfpRule(reasoner));
		rules.put(RuleName.prp_symp, new PrpSympRule(reasoner));
		rules.put(RuleName.prp_asyp, new PrpAsypRule(reasoner));
		rules.put(RuleName.prp_trp, new PrpTrpRule(reasoner));
		rules.put(RuleName.prp_spo_1, new PrpSpo1Rule(reasoner));
		//rules.put(RuleName.prp_spo_2, new PrpSpo2Rule(reasoner));
		rules.put(RuleName.prp_eqp_1, new PrpEqp1Rule(reasoner));
		rules.put(RuleName.prp_eqp_2, new PrpEqp2Rule(reasoner));
		rules.put(RuleName.prp_pdw, new PrpPdwRule(reasoner));
		rules.put(RuleName.prp_adw, new PrpAdwRule(reasoner));
		rules.put(RuleName.prp_inv_1, new PrpInv1Rule(reasoner));
		rules.put(RuleName.prp_inv_2, new PrpInv2Rule(reasoner));
		rules.put(RuleName.prp_key, new PrpKeyRule(reasoner));
		rules.put(RuleName.prp_npa_1, new PrpNpa1Rule(reasoner));
		rules.put(RuleName.prp_npa_2, new PrpNpa2Rule(reasoner));
		rules.put(RuleName.cls_thing, new ClsThingRule(reasoner));
		rules.put(RuleName.cls_nothing_1, new ClsNothing1Rule(reasoner));
		rules.put(RuleName.cls_uni, new ClsUniRule(reasoner));
		rules.put(RuleName.cls_com, new ClsComRule(reasoner));
		rules.put(RuleName.cls_svf_1, new ClsSvf1Rule(reasoner));
		rules.put(RuleName.cls_svf_2, new ClsSvf2Rule(reasoner));
		rules.put(RuleName.cls_avf, new ClsAvfRule(reasoner));
		rules.put(RuleName.cls_hv_1, new ClsHv1Rule(reasoner));
		rules.put(RuleName.cls_hv_2, new ClsHv2Rule(reasoner));
		rules.put(RuleName.cls_maxc_1, new ClsMaxc1Rule(reasoner));
		rules.put(RuleName.cls_maxc_2, new ClsMaxc2Rule(reasoner));
		rules.put(RuleName.cls_maxqc_1, new ClsMaxqc1Rule(reasoner));
		rules.put(RuleName.cls_maxqc_2, new ClsMaxqc2Rule(reasoner));
		rules.put(RuleName.cls_maxqc_3, new ClsMaxqc3Rule(reasoner));
		rules.put(RuleName.cls_maxqc_4, new ClsMaxqc4Rule(reasoner));
		rules.put(RuleName.cls_oo, new ClsOoRule(reasoner));
		rules.put(RuleName.cax_eqc_2, new CaxEqc2Rule(reasoner));
		rules.put(RuleName.cax_dw, new CaxDwRule(reasoner));
		rules.put(RuleName.scm_sco, new ScmScoRule(reasoner));
		rules.put(RuleName.scm_eqc_1, new ScmEqc1Rule(reasoner));
		rules.put(RuleName.scm_eqc_2, new ScmEqc2Rule(reasoner));
		rules.put(RuleName.scm_op_sub, new ScmOpSubRule(reasoner));
		rules.put(RuleName.scm_op_eq, new ScmOpEqRule(reasoner));
		rules.put(RuleName.scm_dp_sub, new ScmDpSubRule(reasoner));
		rules.put(RuleName.scm_dp_eq, new ScmDpEqRule(reasoner));
		rules.put(RuleName.scm_spo, new ScmSpoRule(reasoner));
		rules.put(RuleName.scm_eqp_1, new ScmEqp1Rule(reasoner));
		rules.put(RuleName.scm_eqp_2, new ScmEqp2Rule(reasoner));
		rules.put(RuleName.scm_dom_1, new ScmDom1Rule(reasoner));
		rules.put(RuleName.scm_dom_2, new ScmDom2Rule(reasoner));
		rules.put(RuleName.scm_rng_1, new ScmRng1Rule(reasoner));
		rules.put(RuleName.scm_rng_2, new ScmRng2Rule(reasoner));
		rules.put(RuleName.scm_hv, new ScmHvRule(reasoner));
		rules.put(RuleName.scm_svf_2, new ScmSvf2Rule(reasoner));
		rules.put(RuleName.scm_svf_1, new ScmSvf1Rule(reasoner));
		rules.put(RuleName.scm_avf_1, new ScmAvf1Rule(reasoner));
		rules.put(RuleName.scm_avf_2, new ScmAvf2Rule(reasoner));
		rules.put(RuleName.scm_int, new ScmIntRule(reasoner));
		rules.put(RuleName.scm_uni, new ScmUniRule(reasoner));
		
	};
	
}
