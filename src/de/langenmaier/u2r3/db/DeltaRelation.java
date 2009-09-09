package de.langenmaier.u2r3.db;


/**
 * This class specifies a certain delta of a relation.
 * @author stefan
 *
 */
public class DeltaRelation {
	private long delta;
	private Relation relation;
	public final static long NO_DELTA = -1;
	
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
	
	public DeltaRelation(Relation relation) {
		this.relation = relation;
		delta = relation.getNewDelta();
		relation.createDelta(delta);
	}
	
	public DeltaRelation(Relation relation, long delta) {
		this.relation = relation;
		this.delta = delta;
		//does the relation already exist?
		if (delta != NO_DELTA && delta > relation.getDelta()) {
			relation.createDelta(delta);
		}
		
	}
	
	public long getDelta() {
		return delta;
	}
	
	public Relation getRelation() {
		return relation;
	}

	public DeltaRelation getNextDelta() {
		return new DeltaRelation(relation, delta+1);
	}


//	public void clear() {
//		if (relation != null) {
//			relation.clear(delta);
//		}
//		
//	}
}
