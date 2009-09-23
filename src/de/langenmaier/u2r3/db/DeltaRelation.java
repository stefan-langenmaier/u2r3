package de.langenmaier.u2r3.db;

import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;


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
		return relation.hashCode() ^ (int) delta;
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

	/*public DeltaRelation getNextDelta() {
		return new DeltaRelation(relation, delta+1);
	}*/
	
	/**
	 * Sollte nur im collective Modus benutzt werden
	 */
	public static DeltaRelation getCurrentDelta(Relation relation) {
		if (Settings.getDeltaIteration() != DeltaIteration.COLLECTIVE) {
			throw new U2R3RuntimeException();
		}
		///XXX das sollte eigentlich nicht getNewDelta sein
		return new DeltaRelation(relation, relation.getNewDelta());
	}
	
	public String getDeltaName () {
		return relation.getDeltaName(delta);
	}
	
	public String getTableName () {
		return relation.getTableName();
	}

}
