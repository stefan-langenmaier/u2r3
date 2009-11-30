package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * This class creates setups special functions in the database.
 * @author stefan
 *
 */
public class DBSetup {
	static Logger logger = Logger.getLogger(DBSetup.class);
	
	private static Connection conn = U2R3DBConnection.getConnection();

	private static PreparedStatement dropStatement;

	private static PreparedStatement createStatement;
	
	public static void setup() {
		try {
			dropStatement = conn.prepareStatement("DROP ALIAS isSameLiteral IF EXISTS");
			dropStatement.execute();
		
			createStatement = conn.prepareStatement("CREATE ALIAS isSameLiteral FOR \"de.langenmaier.u2r3.util.DatatypeCheck.isSameLiteral\"");
			createStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
