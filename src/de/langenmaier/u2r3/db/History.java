package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.util.Settings;

/**
 * This class stores how facts have been inferred and manages in which order facts can be deleted.
 * @author stefan
 *
 */
public class History {
	static Logger logger = Logger.getLogger(History.class);
	
	private Connection conn = null;
	private PreparedStatement createStatement = null;
	private PreparedStatement dropStatement = null;
	private Statement stmt = null;
	
	protected History() {
		conn = U2R3DBConnection.getConnection();
		try {
			if (Settings.startClean()) {
				dropStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS");
				dropStatement.execute();
			
				createStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID NOT NULL, table INTEGER, sourceId UUID, sourceTable INTEGER, PRIMARY KEY (id, sourceId))");
				createStatement.execute();
			}
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getTableName() {
		return "history";
	}

	public void add(String sql) {
		logger.trace("Adding history data from (" + sql + ")");
		
		try {
			stmt.execute("INSERT INTO history (id, table, sourceId, sourceTable) " + sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.trace("Added history data from (" + sql + ")");
		
	}
}
