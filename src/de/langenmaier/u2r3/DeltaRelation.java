package de.langenmaier.u2r3;

import de.langenmaier.u2r3.db.Relation;

/**
 * This class specifies a certain delta of a relation.
 * @author stefan
 *
 */
public class DeltaRelation {
	long delta;
	Relation relation;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeltaRelation) {
			DeltaRelation tmp = (DeltaRelation) obj;
			if (tmp.delta == delta && relation.equals(tmp.relation)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return relation.hashCode() & (int) delta;
	}

	public void clear() {
		if (relation != null) {
			relation.clear(delta);
		}
		
	}
}
