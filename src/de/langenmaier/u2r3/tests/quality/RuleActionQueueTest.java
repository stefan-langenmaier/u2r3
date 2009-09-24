package de.langenmaier.u2r3.tests.quality;

import de.langenmaier.u2r3.util.RuleAction;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.RuleManager.RuleName;
import de.langenmaier.u2r3.util.RuleActionQueue;
import junit.framework.TestCase;

public class RuleActionQueueTest extends TestCase {

	public void testActivate() {
		RuleActionQueue aq = new RuleActionQueue();
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleName.eq_trans), 
				RelationManager.getRelation(RelationName.classAssertion).createNewDeltaRelation());
		
		aq.add(ra);
		assertEquals(1, aq.size());
		aq.activate();
		assertEquals(1, aq.size());
	}

	public void testDelete() {
		RuleActionQueue aq = new RuleActionQueue();
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleName.eq_trans), 
				RelationManager.getRelation(RelationName.classAssertion).createNewDeltaRelation());
		
		aq.add(ra);
		assertEquals(1, aq.size());
		aq.activate();
		assertEquals(1, aq.size());
		aq.delete(ra);
		assertTrue(aq.isEmpty());
	}

	public void testAdd() {
		RuleActionQueue aq = new RuleActionQueue();
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleName.eq_trans), 
				RelationManager.getRelation(RelationName.classAssertion).createNewDeltaRelation());
		
		aq.add(ra);		
		assertEquals(1, aq.size());
		
		aq.add(ra);		
		assertEquals(1, aq.size());
		assertEquals(1d, aq.activate().getWeight());
		

	}

}
