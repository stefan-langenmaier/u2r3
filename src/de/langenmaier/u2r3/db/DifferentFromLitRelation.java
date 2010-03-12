package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import de.langenmaier.u2r3.core.U2R3Reasoner;

public class DifferentFromLitRelation extends Relation {
	
	protected DifferentFromLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "differentFromLit";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colLeft TEXT," +
		" colRight TEXT," +
		" left_language TEXT," +
		" left_type TEXT," +
		" right_language TEXT," +
		" right_type TEXT," +
		" PRIMARY KEY (id, colLeft, colRight));" +
		" CREATE INDEX " + table + "_left ON " + table + "(colLeft);" +
		" CREATE INDEX " + table + "_right ON " + table + "(colRight);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colLeft, colRight, left_language, left_type, right_language, right_type) VALUES (?, ?, ?, ?, ?, ?)";
	}


}
