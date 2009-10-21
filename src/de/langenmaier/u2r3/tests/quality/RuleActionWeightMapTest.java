package de.langenmaier.u2r3.tests.quality;

import org.semanticweb.owlapi.inference.OWLReasonerException;

import de.langenmaier.u2r3.util.RuleAction;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.RuleManager.RuleName;
import de.langenmaier.u2r3.util.RuleActionWeightMap;
import junit.framework.TestCase;

public class RuleActionWeightMapTest extends TestCase {

	public void testPutRuleAction() {
		RuleActionWeightMap wp = new RuleActionWeightMap();
		
		U2R3Reasoner reasoner = null;
		try {
			reasoner = new U2R3Reasoner(null, null);
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA));
		wp.put(ra);
		System.out.println(wp);System.out.println(wp.get(ra));
		
		assertEquals(0.0d, wp.get(ra));
		
		wp.put(ra);
		System.out.println(wp);System.out.println(wp.get(ra));
		
		assertEquals(0.0d+1, wp.get(ra));
		
		RuleAction ra2 = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent),
				relationManager.getRelation(RelationName.classAssertionEnt).createNewDeltaRelation());
		wp.put(ra2);
		System.out.println(wp);System.out.println(wp.get(ra2));
		
		assertEquals(0.0d, wp.get(ra2));
	}

}
