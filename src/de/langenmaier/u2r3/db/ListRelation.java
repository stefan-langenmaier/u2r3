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
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" name TEXT," +
					" element TEXT," +
					" PRIMARY KEY (name, element));" +
					" CREATE INDEX " + getTableName() + "_name ON " + getTableName() + "(name);" +
					" CREATE INDEX " + getTableName() + "_element ON " + getTableName() + "(element)");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (name, element) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
