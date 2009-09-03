package de.langenmaier.u2r3.tests.quality;

import de.langenmaier.u2r3.RuleAction;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.util.RuleActionDeltaMap;
import junit.framework.TestCase;

public class RuleActionDeltaMapTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPutRuleAction() {
		RuleActionDeltaMap dm = new RuleActionDeltaMap();
		
		dm.put(new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.subClass), DeltaRelation.NO_DELTA)));
		assertEquals(1, dm.size());
		
		dm.put(new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.subClass), DeltaRelation.NO_DELTA)));
		
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion)));
		System.out.println(ra.hashCode());
		dm.put(ra);
		assertEquals(2, dm.size());
		
		ra = new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion)));
		System.out.println(ra.hashCode());
		dm.put(ra);
		assertEquals(3, dm.size());
		
		//TODO  weitere Regeln implementieren
		System.out.println(dm);
		
	}

	public void testReduce() {
		RuleActionDeltaMap dm = new RuleActionDeltaMap();
		
		RuleAction ra2 = new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.subClass), DeltaRelation.NO_DELTA));
		
		dm.put(ra2);
		assertEquals(1, dm.size());
		
		dm.put(new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.subClass), DeltaRelation.NO_DELTA)));
		
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass), 
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion)));
		System.out.println(ra.hashCode());
		dm.put(ra);
		assertEquals(2, dm.size());
		
		assertTrue(dm.reduce(ra));
		
		assertFalse(dm.reduce(ra2));
		
		assertTrue(dm.reduce(ra2));
	}

}
