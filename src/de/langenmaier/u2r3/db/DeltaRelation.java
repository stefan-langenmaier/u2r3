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
		if (relation instanceof MergeableRelation) {
			MergeableRelation mrelation = (MergeableRelation) relation;
			return mrelation.getDeltaName(delta);
		} else {
			return relation.getTableName();
		}
	}
	
	public String getTableName () {
		return relation.getTableName();
	}

	public void dispose() {
		if (relation instanceof MergeableRelation) {
			MergeableRelation mrelation = (MergeableRelation) relation;
			mrelation.removeDeltaRelation(delta);
		}
	}

	public String getDeltaName(String table) {
		if (relation instanceof MergeableRelation) {
			MergeableRelation mrelation = (MergeableRelation) relation;
			return mrelation.getDeltaName(delta, table);
		} else {
			return relation.getTableName();
		}
	}
//	
//	public String getDeltaName(int delta, String table) {
//		if (!table.equals(relation.getTableName())) return table;
//		if (delta == DeltaRelation.NO_DELTA) {
//			return getTableName();
//		}
//		return getTableName() + "_d" + delta;
//	}
	
	public void merge(DeltaRelation delta) {
		if (relation instanceof MergeableRelation) {
			MergeableRelation mrelation = (MergeableRelation) relation;
			mrelation.merge(delta);
		}
	}
	
	public void makeDirty() {
		if (relation instanceof MergeableRelation) {
			MergeableRelation mrelation = (MergeableRelation) relation;
			mrelation.makeDirty();
		}
	}


}
