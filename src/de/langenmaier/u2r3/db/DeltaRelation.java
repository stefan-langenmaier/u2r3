package de.langenmaier.u2r3.db;

/**
 * This class specifies a certain delta of a relation.
 * @author stefan
 *
 */
public class DeltaRelation {
	private int delta;
	private Relation relation;
	public final static int NO_DELTA = -1;
	
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
	
	public static DeltaRelation getNoDelta(Relation relation) {
		return new DeltaRelation(relation, NO_DELTA);
	}
	
	@Override
	public int hashCode() {
		return relation.hashCode() ^ (int) delta;
	}
	
	public DeltaRelation(Relation relation, int delta) {
		this.relation = relation;
		this.delta = delta;
	}
	
	public long getDelta() {
		return delta;
	}
	
	public Relation getRelation() {
		return relation;
	}
	
	public String getDeltaName() {
		return relation.getDeltaName(delta);
	}
	
	public String getTableName () {
		return relation.getTableName();
	}

	public void dispose() {
		relation.removeDeltaRelation(delta);
	}

	public String getDeltaName(String table) {
		return relation.getDeltaName(delta, table);
	}

	public void merge(DeltaRelation delta) {
		relation.merge(delta);
	}
	
	public void makeDirty() {
		relation.makeDirty();
	}


}
