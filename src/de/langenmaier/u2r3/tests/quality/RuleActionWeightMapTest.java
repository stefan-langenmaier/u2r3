package de.langenmaier.u2r3.tests.quality;

import de.langenmaier.u2r3.util.RuleAction;
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
		
		RuleAction ra = new RuleAction(RuleManager.getRule(RuleName.eq_trans),
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion), DeltaRelation.NO_DELTA));
		wp.put(ra);
		System.out.println(wp);System.out.println(wp.get(ra));
		
		assertEquals(0.0d, wp.get(ra));
		
		wp.put(ra);
		System.out.println(wp);System.out.println(wp.get(ra));
		
		assertEquals(0.0d+1, wp.get(ra));
		
		RuleAction ra2 = new RuleAction(RuleManager.getRule(RuleName.eq_trans),
				new DeltaRelation(RelationManager.getRelation(RelationName.classAssertion)));
		wp.put(ra2);
		System.out.println(wp);System.out.println(wp.get(ra2));
		
		assertEquals(0.0d, wp.get(ra2));
	}

}
