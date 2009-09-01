package de.langenmaier.u2r3.tests.quality;

import de.langenmaier.u2r3.RuleAction;
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
		
		dm.put(new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass)));
		assertEquals(1, dm.size());
		
		dm.put(new RuleAction(RuleManager.getRule(RuleManager.RuleName.transSubClass)));
		assertEquals(1, dm.size());
		
		System.out.println(dm);
		
	}

	public void testReduce() {
		fail("Not yet implemented");
	}

}
