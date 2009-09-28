package de.langenmaier.u2r3.tests.quality;

import org.semanticweb.owlapi.inference.OWLReasonerException;

import de.langenmaier.u2r3.util.RuleAction;
import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.RuleManager.RuleName;
import de.langenmaier.u2r3.util.RuleActionQueue;
import junit.framework.TestCase;

public class RuleActionQueueTest extends TestCase {

	public void testActivate() {
		U2R3Reasoner reasoner = null;
		try {
			reasoner = new U2R3Reasoner(null, null);
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		RuleActionQueue aq = new RuleActionQueue(reasoner);
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans), 
				relationManager.getRelation(RelationName.classAssertion).createNewDeltaRelation());
		
		aq.add(ra);
		assertEquals(1, aq.size());
		aq.activate();
		assertEquals(1, aq.size());
	}

	public void testDelete() {
		U2R3Reasoner reasoner = null;
		try {
			reasoner = new U2R3Reasoner(null, null);
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		RuleActionQueue aq = new RuleActionQueue(reasoner);
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans), 
				relationManager.getRelation(RelationName.classAssertion).createNewDeltaRelation());
		
		aq.add(ra);
		assertEquals(1, aq.size());
		aq.activate();
		assertEquals(1, aq.size());
		aq.delete(ra);
		assertTrue(aq.isEmpty());
	}

	public void testAdd() {
		U2R3Reasoner reasoner = null;
		try {
			reasoner = new U2R3Reasoner(null, null);
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		RuleActionQueue aq = new RuleActionQueue(reasoner);
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans), 
				relationManager.getRelation(RelationName.classAssertion).createNewDeltaRelation());
		
		aq.add(ra);		
		assertEquals(1, aq.size());
		
		aq.add(ra);		
		assertEquals(1, aq.size());
		assertEquals(1d, aq.activate().getWeight());
		

	}

}
