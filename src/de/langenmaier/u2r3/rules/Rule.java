package de.langenmaier.u2r3.rules;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.U2R3DBConnection;


/**
 * The rule class implements the semantic to create new data from a specific OWL2 RL rule. The rule class itself is abstract, cause there are only specific rules, but all should have a common interface.
 * @author stefan
 *
 */
public abstract class Rule {
	protected Connection conn = null;
	protected Statement statement = null;
	protected Rule() {
		conn = U2R3DBConnection.getConnection();
		try {
			statement = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public abstract void apply(DeltaRelation delta);
	
	public abstract String toString();
}
