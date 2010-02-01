package de.langenmaier.u2r3.util;

import java.util.HashSet;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.MergeableRelation;
import de.langenmaier.u2r3.db.Relation;
import de.langenmaier.u2r3.rules.Rule;

public class AdditionReason extends Reason {

	public AdditionReason(Relation relation) {
		super(relation);
	}
	
	public AdditionReason(MergeableRelation relation, DeltaRelation delta) {
		super(relation, delta);
	}

	public HashSet<Rule> getRules() {
		return getRelation().getAdditionRules();
	}

}
