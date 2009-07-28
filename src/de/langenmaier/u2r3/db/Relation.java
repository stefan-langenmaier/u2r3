package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.semanticweb.owl.model.OWLAxiom;

/**
 * Contains the default methods that a relation should contain and tries to do much of the default connection stuff.
 * @author stefan
 *
 */
public abstract class Relation {

	protected Connection conn = null;
	protected PreparedStatement addStatement;
	protected PreparedStatement createStatement;
	protected PreparedStatement dropStatement;
	
	protected Relation() {
		conn = U2R3DBConnection.getConnection();
	}
	
	public abstract void add(OWLAxiom axiom);
	protected abstract void create();

}
