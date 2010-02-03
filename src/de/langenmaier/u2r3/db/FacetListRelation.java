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
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" name TEXT," +
					" facet TEXT," +
					" value TEXT," +
					" type TEXT," +
					" language TEXT," +
					" PRIMARY KEY (name, facet, value, type, language));" +
					" CREATE INDEX " + getTableName() + "_name ON " + getTableName() + "(name);" +
					" CREATE INDEX " + getTableName() + "_facet ON " + getTableName() + "(facet);" +
					" CREATE INDEX " + getTableName() + "_value ON " + getTableName() + "(value);" +
					" CREATE INDEX " + getTableName() + "_type ON " + getTableName() + "(type);" +
					" CREATE INDEX " + getTableName() + "_language ON " + getTableName() + "(language)");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (name, facet, value, type, language) VALUES (?, ?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
