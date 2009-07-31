package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;

import de.langenmaier.u2r3.Settings;
import de.langenmaier.u2r3.rules.Rule;

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
	protected PreparedStatement createAuxStatement;
	protected PreparedStatement dropAuxStatement;
	protected PreparedStatement createDeltaStatement;
	protected PreparedStatement dropDeltaStatement;
	
	
	protected String tableName;
	
	protected HashSet<Rule> rules = new HashSet<Rule>();
	
	protected boolean isDirty = false;
	
	protected Relation() {
		conn = U2R3DBConnection.getConnection();
	}
	
	public abstract void add(OWLAxiom axiom);
	
	public HashSet<Rule> getRules() {
		return rules;
	}
	
	protected void create() {
		try {
			if (Settings.startClean()) {
				dropMainStatement.execute();
				dropAuxStatement.execute();
				dropDeltaStatement.execute();
				createMainStatement.executeUpdate();
				createAuxStatement.execute();
				createDeltaStatement.execute();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	/**
	 * Aux ins delta packen, und delta hinzufügen
	 */
	public void merge() {
		try {
			Statement stmt = conn.createStatement();
			//was in delta steht muss erst mal in die haupttabellen
			logger.debug("INSERT INTO " + tableName + "  SELECT * FROM " + tableName + "Delta");
			stmt.execute("INSERT INTO " + tableName + "  SELECT * FROM " + tableName + "Delta");
			
			//dann muss das delta gelöscht werden
			logger.debug("DELETE FROM " + tableName + "Delta");
			stmt.execute("DELETE FROM " + tableName + "Delta");
			
			//dann können die neuen daten ins delta
			logger.debug("INSERT INTO " + tableName + "Delta  SELECT * FROM (SELECT * FROM " + tableName + "Aux EXCEPT SELECT * FROM " + tableName+ ")");
			stmt.execute("INSERT INTO " + tableName + "Delta  SELECT * FROM (SELECT * FROM " + tableName + "Aux EXCEPT SELECT * FROM " + tableName+ ")");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
