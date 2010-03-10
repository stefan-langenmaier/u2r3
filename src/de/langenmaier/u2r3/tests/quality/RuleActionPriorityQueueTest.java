package de.langenmaier.u2r3.tests.quality;

import junit.framework.TestCase;
import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.RuleManager.RuleName;
import de.langenmaier.u2r3.util.RuleAction;
import de.langenmaier.u2r3.util.RuleActionPriorityQueue;

public class RuleActionPriorityQueueTest extends TestCase {

	public void testAddRuleAction() {
		RuleActionPriorityQueue pq = new RuleActionPriorityQueue();
		
		U2R3Reasoner reasoner = null;
		reasoner = (U2R3Reasoner) new U2R3ReasonerFactory().createReasoner(null);
		
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA));
		pq.add(ra);
		
		assertEquals(1, pq.size());
		
		pq.add(ra);
		assertEquals(1, pq.size());
		
		RuleAction ra2 = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent),
				relationManager.getRelation(RelationName.classAssertionEnt).createDeltaRelation(DeltaRelation.NO_DELTA));
		ra2.setWeight(2.0d);
		pq.add(ra2);
		assertEquals(1, pq.size());
		assertEquals(2.0d, pq.remove().getWeight());
		pq.add(ra2);
		
		RuleAction ra3 = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent),
				relationManager.getRelation(RelationName.classAssertionEnt).createNewDeltaRelation());
		pq.add(ra3);
		assertEquals(2, pq.size());
	}


}
