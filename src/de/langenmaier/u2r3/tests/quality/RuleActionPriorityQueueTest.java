package de.langenmaier.u2r3.tests.quality;

import de.langenmaier.u2r3.util.RuleAction;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.RuleManager.RuleName;
import de.langenmaier.u2r3.util.RuleActionPriorityQueue;
import junit.framework.TestCase;

public class RuleActionPriorityQueueTest extends TestCase {

	public void testAddRuleAction() {
		RuleActionPriorityQueue pq = new RuleActionPriorityQueue();
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleName.eq_trans),
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion), DeltaRelation.NO_DELTA));
		pq.add(ra);
		
		assertEquals(1, pq.size());
		
		pq.add(ra);
		assertEquals(1, pq.size());
		
		RuleAction ra2 = new RuleAction(RuleManager.getRule(RuleName.eq_trans),
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion), DeltaRelation.NO_DELTA));
		ra2.setWeight(2.0d);
		pq.add(ra2);
		assertEquals(1, pq.size());
		assertEquals(2.0d, pq.remove().getWeight());
		pq.add(ra2);
		
		RuleAction ra3 = new RuleAction(RuleManager.getRule(RuleName.eq_trans),
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion)));
		pq.add(ra3);
		assertEquals(2, pq.size());
	}


}
