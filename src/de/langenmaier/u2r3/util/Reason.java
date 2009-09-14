package de.langenmaier.u2r3.util;

import java.util.HashSet;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.Relation;
import de.langenmaier.u2r3.rules.Rule;

/**
 * The Reason class contains the information why something should be updated. This mean what has the data change triggerd.
 * @author stefan
 *
 */
public abstract class Reason {
	private Relation relation;
	private DeltaRelation delta;
	
	public Reason(Relation relation, DeltaRelation delta) {
		this.relation = relation;
		this.delta = delta;
	}
	
	public Reason(Relation relation) {
		this.relation = relation;
		this.delta = null;
	}

	public DeltaRelation getDelta() {
		return delta;
	}

	public Relation getRelation() {
		return relation;
	}

	public abstract HashSet<Rule> getRules();
}
