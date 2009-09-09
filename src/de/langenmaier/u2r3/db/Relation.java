package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

/**
 * Contains the default methods that a relation should contain and tries
 * to do much of the default connection stuff.
 * @author stefan
 *
 */
public abstract class Relation {
	static Logger logger = Logger.getLogger(Relation.class);
	
	protected Connection conn = null;
	protected PreparedStatement addStatement;
	protected PreparedStatement createMainStatement;
	protected PreparedStatement dropMainStatement;
	protected Statement createDeltaStatement;
	protected Statement dropDeltaStatement;
	
	private long nextDelta = 0;
	
	protected String tableName;
	
	protected HashSet<Rule> rules = new HashSet<Rule>();

	protected boolean isDirty = false;
	
	protected Relation() {
		conn = U2R3DBConnection.getConnection();
		try {
			createDeltaStatement = conn.createStatement();
			dropDeltaStatement = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void add(OWLAxiom axiom);
	
	public HashSet<Rule> getRules() {
		return rules;
	}
	
	public void addRules(Rule rule) {
		rules.add(rule);
	}
	
	protected void create() {
		try {
			if (Settings.startClean()) {
				dropMainStatement.execute();
				createMainStatement.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void createDeltaImpl(long id);
	
	public void createDelta(long id) {
		if (id >= nextDelta) {
			++nextDelta;
			createDeltaImpl(id);
		}
	}
	
	public abstract void dropDelta(long id);
	
	protected synchronized long getNewDelta() {
		return nextDelta;
	}
	
	protected synchronized long getDelta() {
		return nextDelta-1;
	}

	/**
	 * Should only be used in the collective Mode
	 */
	public void makeDirty() {
		if (Settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
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
		merge(new DeltaRelation(this, this.getDelta()));
		long pd = getPreviousDelta();
		if (pd != DeltaRelation.NO_DELTA) {
			dropDelta(getPreviousDelta());
		}
	}

	/**
	 * This method is only reasonable in the context of an execution
	 * in the collective mode
	 * @return
	 */
	private long getPreviousDelta() {
		return nextDelta-2;
	}

	protected String getTableName() {
		return tableName;
	}
	
	protected String getDeltaName(long delta) {
		return getTableName() + "_d" + delta;
	}
}
