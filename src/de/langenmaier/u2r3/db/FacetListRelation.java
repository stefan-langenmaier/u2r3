package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;

public class FacetListRelation extends Relation {
	static Logger logger = Logger.getLogger(FacetListRelation.class);
	
	protected FacetListRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "facetList";
			
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
		" facet TEXT," +
		" value TEXT," +
		" type TEXT," +
		" language TEXT," +
		" PRIMARY KEY (name, facet, value, type, language));" +
		" CREATE INDEX " + table + "_name ON " + table + "(name);" +
		" CREATE INDEX " + table + "_facet ON " + table + "(facet);" +
		" CREATE INDEX " + table + "_value ON " + table + "(value);" +
		" CREATE INDEX " + table + "_type ON " + table + "(type);" +
		" CREATE INDEX " + table + "_language ON " + table + "(language)";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (name, facet, value, type, language) VALUES (?, ?, ?, ?, ?)";
	}


}
