package de.langenmaier.u2r3;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.Relation;

/**
 * The Reason class contains the information why something should be updated. This mean what has the data change triggerd.
 * @author stefan
 *
 */
public class Reason {
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
}
