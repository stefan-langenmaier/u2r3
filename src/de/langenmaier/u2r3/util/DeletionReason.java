package de.langenmaier.u2r3.util;

import java.util.HashSet;

import de.langenmaier.u2r3.db.Relation;
import de.langenmaier.u2r3.rules.Rule;

public class DeletionReason extends Reason {

	public DeletionReason(Relation relation) {
		super(relation);
	}
	
	// cannot be applied to deltas
	/*public DeletionReason(Relation relation, DeltaRelation delta) {
		super(relation, delta);
	}*/

	public HashSet<Rule> getRules() {
		return getRelation().getDeletionRules();
	}

}
