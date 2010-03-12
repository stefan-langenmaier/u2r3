package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;

public class ListRelation extends Relation {
	static Logger logger = Logger.getLogger(ListRelation.class);
	
	protected ListRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "list";
			
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
		" name TEXT," +
		" element TEXT," +
		" PRIMARY KEY (id));" +
		" CREATE INDEX " + table + "_name ON " + table + "(name);" +
		" CREATE INDEX " + table + "_element ON " + table + "(element)";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (name, element) VALUES (?, ?)";
	}

}
