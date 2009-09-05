package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.Settings;

/**
 * Contains the default methods that a relation should contain and tries to do much of the default connection stuff.
 * @author stefan
 *
 */
public abstract class Relation {
	static Logger logger = Logger.getLogger(Relation.class);
	
	protected Connection conn = null;
	protected PreparedStatement addStatement;
	protected PreparedStatement createMainStatement;
	protected PreparedStatement dropMainStatement;
//	protected PreparedStatement createAuxStatement;
//	protected PreparedStatement dropAuxStatement;
	protected Statement createDeltaStatement;
	protected Statement dropDeltaStatement;
	
	private long lastDelta = 0;
	
	
	protected String tableName;
	
	protected HashSet<Rule> rules = new HashSet<Rule>();
	//private Vector<DeltaRelation> deltas = new Vector<DeltaRelation>();

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
//				dropAuxStatement.execute();
//				dropDeltaStatement.execute();
				createMainStatement.executeUpdate();
//				createAuxStatement.execute();
//				createDeltaStatement.execute();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void createDelta(long id);
	
	public abstract void dropDelta(long id);
	
	protected synchronized long getNewDelta() {
		++lastDelta;
		return lastDelta-1;
	}
	
	protected synchronized long getDelta() {
		return lastDelta;
	}

	public void makeDirty() {
		isDirty = true;
		
	}
	
	public abstract void merge(DeltaRelation delta) ;
	
	/*public void setDirty(boolean dirty) {
		isDirty = dirty;
	}*/
	
	public boolean isDirty() {
		return isDirty;
	}

	public void merge() {
		merge(new DeltaRelation(this, this.getDelta()));
		
	}
	
	/**
	 * Aux ins delta packen, und delta hinzufügen
	 *//*
	public long merge() {
		try {
			long rows = 0;
			Statement stmt = conn.createStatement();
	
			//das zu letzt bearbeitet delta is fertig bearbeitet
			logger.debug("DELETE FROM " + tableName + "Delta");
			stmt.execute("DELETE FROM " + tableName + "Delta");
			
			//die neu erarbeiteten daten können ins delta
			logger.debug("INSERT INTO " + tableName + "Delta  SELECT * FROM (SELECT * FROM " + tableName + "Aux EXCEPT SELECT * FROM " + tableName+ ")");
			stmt.execute("INSERT INTO " + tableName + "Delta  SELECT * FROM (SELECT * FROM " + tableName + "Aux EXCEPT SELECT * FROM " + tableName+ ")");
			
			//die neuen daten werden auch gleich in die haupttabelle mit aufgenommen
			logger.debug("INSERT INTO " + tableName + "  SELECT * FROM " + tableName + "Delta");
			rows = stmt.executeUpdate("INSERT INTO " + tableName + "  SELECT * FROM " + tableName + "Delta");
			
			//dann muss noch die alte hilfstabelle bereinigt werden
			logger.debug("DELETE FROM " + tableName + "Aux");
			stmt.execute("DELETE FROM " + tableName + "Aux");
			
			//jetzt is wieder alles sauber
			isDirty = false;
			
			return rows;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	

	public void clear(long delta) {
		// TODO CLEAR delta
		
	}*/

}
