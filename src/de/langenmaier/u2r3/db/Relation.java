package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3ReasonerException;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.U2R3Component;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

/**
 * Contains the default methods that a relation should contain and tries
 * to do much of the default connection stuff.
 * @author stefan
 *
 */
public abstract class Relation extends U2R3Component {
	static Logger logger = Logger.getLogger(Relation.class);
	
	protected Connection conn = null;
	protected PreparedStatement addStatement;
	protected PreparedStatement createMainStatement;
	protected PreparedStatement dropMainStatement;
	protected Statement createDeltaStatement;
	protected Statement dropDeltaStatement;

	
	private int nextDelta = 0;
	private HashMap<Integer, DeltaRelation> deltas = new HashMap<Integer, DeltaRelation>();
	
	protected String tableName;
	
	//rules that should be triggered when something is added to the relation
	protected HashSet<Rule> additionRules = new HashSet<Rule>();
	
	//rules that should be triggered when something is removed from the relation
	protected HashSet<Rule> deletionRules = new HashSet<Rule>();

	protected boolean isDirty = false;
	
	protected Relation(U2R3Reasoner reasoner) {
		super(reasoner);
		conn = U2R3DBConnection.getConnection();
		try {
			createDeltaStatement = conn.createStatement();
			dropDeltaStatement = conn.createStatement();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public abstract void addImpl(OWLAxiom axiom) throws SQLException;
	
	public void add(OWLAxiom axiom) {
		try {
			addImpl(axiom);
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
			reasonProcessor.add(new AdditionReason(this));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public abstract Pair<UUID, RelationName> removeImpl(OWLAxiom axiom) throws SQLException;
	
	public void remove(OWLAxiom axiom) {
		try {
			reasonProcessor.pause();
			
			Pair<UUID, RelationName> res = removeImpl(axiom);

			if (res.getFirst() != null) {
				relationManager.remove(res.getFirst(), res.getSecond());
			}
			
			reasonProcessor.resume();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public HashSet<Rule> getAdditionRules() {
		return additionRules;
	}
	
	public void addAdditionRule(Rule rule) {
		additionRules.add(rule);
	}
	
	public HashSet<Rule> getDeletionRules() {
		return deletionRules;
	}
	
	public void addDeletionRule(Rule rule) {
		deletionRules.add(rule);
	}
	
	protected void create() {
		try {
			if (settings.startClean()) {
				dropMainStatement.execute();
				createMainStatement.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void createDeltaImpl(long id);
	
	private void createDelta(long id) {
		//if (id >= nextDelta) {
			if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
				++nextDelta;
			}
			createDeltaImpl(id);
		//} else {
			//throw new U2R3RuntimeException();
		//}
	}
	
	protected void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE " + getDeltaName(id) + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
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

	protected String getTableName() {
		return tableName;
	}
	
	protected String getDeltaName(long delta) {
		if (delta == DeltaRelation.NO_DELTA) {
			return getTableName();
		}
		return getTableName() + "_d" + delta;
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
	
	public DeltaRelation createNewDeltaRelation() {
		return createDeltaRelation(getNewDelta());
	}

	public void removeDeltaRelation(int delta) {
		dropDelta(delta);
		deltas.remove(delta);
	}


	public boolean exists(String... args) throws U2R3ReasonerException {
		//TODO abstract machen und in die einzelnen Klassen schreiben
		try {
			Statement stmt = conn.createStatement();
			String sql;
			if (args.length == 1) {
				sql = "SELECT class FROM classAssertion WHERE class = '" + args[0] + "'";
			} else {
				sql = "SELECT class, type FROM classAssertion WHERE class = '" + args[0] + "' AND type = '" + args[1] + "'";
			}
			
			return stmt.executeQuery(sql).next();
		} catch (SQLException e) {
			throw new U2R3ReasonerException(e);
		}
	}
}
