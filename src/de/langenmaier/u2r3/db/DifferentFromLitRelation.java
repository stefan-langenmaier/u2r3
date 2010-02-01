package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import de.langenmaier.u2r3.core.U2R3Reasoner;

public class DifferentFromLitRelation extends Relation {
	
	protected DifferentFromLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "differentFromLit";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" left_language TEXT," +
					" left_type TEXT," +
					" right_language TEXT," +
					" right_type TEXT," +
					" PRIMARY KEY (id, colLeft, colRight));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(colLeft);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(colRight);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight, left_language, left_type, right_language, right_type) VALUES (?, ?, ?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
