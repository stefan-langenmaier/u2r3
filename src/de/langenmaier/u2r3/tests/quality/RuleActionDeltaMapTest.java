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
import de.langenmaier.u2r3.util.RuleActionDeltaMap;

public class RuleActionDeltaMapTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPutRuleAction() {
		RuleActionDeltaMap dm = new RuleActionDeltaMap();
		
		U2R3Reasoner reasoner = null;
		reasoner = (U2R3Reasoner) new U2R3ReasonerFactory().createReasoner(null);
		
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		dm.put(new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.subClass).createDeltaRelation(DeltaRelation.NO_DELTA)));
		assertEquals(1, dm.size());
		
		dm.put(new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.subClass).createDeltaRelation(DeltaRelation.NO_DELTA)));
		
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.classAssertionEnt).createNewDeltaRelation());
		System.out.println(ra.hashCode());
		dm.put(ra);
		assertEquals(2, dm.size());
		
		ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.classAssertionEnt).createNewDeltaRelation());
		System.out.println(ra.hashCode());
		dm.put(ra);
		assertEquals(3, dm.size());
		
		//TODO  weitere Regeln implementieren
		System.out.println(dm);
		
	}

	public void testReduce() {
		RuleActionDeltaMap dm = new RuleActionDeltaMap();
		
		U2R3Reasoner reasoner = null;
		reasoner = (U2R3Reasoner) new U2R3ReasonerFactory().createReasoner(null);
		
		RuleManager ruleManager = reasoner.getRuleManager();
		RelationManager relationManager = reasoner.getRelationManager();
		
		RuleAction ra2 = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.subClass).createDeltaRelation(DeltaRelation.NO_DELTA));
		
		dm.put(ra2);
		assertEquals(1, dm.size());
		
		dm.put(new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.subClass).createDeltaRelation(DeltaRelation.NO_DELTA)));
		
		
		RuleAction ra = new RuleAction(ruleManager.getRule(RuleName.eq_trans_ent), 
				relationManager.getMergeableRelation(RelationName.classAssertionEnt).createNewDeltaRelation());
		System.out.println(ra.hashCode());
		dm.put(ra);
		assertEquals(2, dm.size());
		
		assertTrue(dm.reduce(ra));
		
		assertFalse(dm.reduce(ra2));
		
		assertTrue(dm.reduce(ra2));
	}

}
