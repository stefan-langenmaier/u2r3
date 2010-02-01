package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

/**
 * Only a small part of all relations has deltas to simplify these
 * the code for deltas, their creation and merge is removed in a
 * specialised subclass.
 * @author sl17
 *
 */
public abstract class MergeableRelation extends Relation {
	
	protected Statement createDeltaStatement;
	protected Statement dropDeltaStatement;
	
	private int nextDelta = 0;
	private HashMap<Integer, DeltaRelation> deltas = new HashMap<Integer, DeltaRelation>();

	protected MergeableRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			createDeltaStatement = conn.createStatement();
			dropDeltaStatement = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO Wieso muss das ein Unterklasse machen?
	 * Creates the delta of an relation for reasoning
	 * @param id
	 */
	public abstract void createDeltaImpl(int id);
	
	private void createDelta(int id) {
			if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
				++nextDelta;
			}
			createDeltaImpl(id);
	}
	
	protected void dropDelta(int id) {
		try {
			dropDeltaStatement.execute("DROP TABLE " + getDeltaName(id));
		} catch (SQLException e) {
			logger.warn("Delta '" + getDeltaName(id) + "' konnte nicht geloescht werden.");
			//e.printStackTrace();
		}
	}
	
	protected synchronized int getNewDelta() {
		return nextDelta;
	}
	
	protected synchronized int getDelta() {
		return nextDelta-1;
	}

	/**
	 * Should only be used in the collective Mode
	 */
	public void makeDirty() {
		if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
			throw new RuntimeException("this is not allowed in immediate mode");
		}
		isDirty = true;
		
	}
	
	/**
	 * The delta Relation is merged to the main relation
	 * @param delta
	 */
	public abstract void merge(DeltaRelation delta);
		
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Merges the current delta to the main relation
	 */
	public void merge() {
		merge(deltas.get(getNewDelta()));
		if (getDelta() != DeltaRelation.NO_DELTA) {
			dropDelta(getDelta());
		}
		++nextDelta;
	}
	
	public DeltaRelation createNewDeltaRelation() {
		return createDeltaRelation(getNewDelta());
	}

	public void removeDeltaRelation(int delta) {
		dropDelta(delta);
		deltas.remove(delta);
	}
	
	protected String getDeltaName(int delta) {
		return getDeltaName(delta, tableName);
	}
	
	public DeltaRelation createDeltaRelation(int delta) {
		if (!deltas.containsKey(delta)) {
			if (delta != DeltaRelation.NO_DELTA) {
				createDelta(delta);
			}
			deltas.put(delta, new DeltaRelation(this, delta));
		}
		DeltaRelation deltaRelation = deltas.get(delta);
		return deltaRelation;
	}
	
	public String getDeltaName(int delta, String table) {
		if (!table.equals(tableName)) return table;
		if (delta == DeltaRelation.NO_DELTA) {
			return getTableName();
		}
		return getTableName() + "_d" + delta;
	}


}
