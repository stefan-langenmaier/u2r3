package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * This class setups special functions in the database.
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

			/*
			 * Henne-Ei-Problem
			 * Die Sequence kann erste geloescht werden wenn alle
			 * Abhaengigkeiten beseitigt sind. Aber um neue Tabellen
			 * anzulegen muss sie wieder vorhanden sein. Man kann also
			 * den Aufruf nicht einfach ans Ende stellen.
			 */
			dropStatement = conn.prepareStatement("DROP SEQUENCE uid");
			try {
				dropStatement.execute();
			} catch (SQLException e) {
				logger.warn("Sequence has NOT been deleted.");
			}

			createStatement = conn.prepareStatement("CREATE SEQUENCE uid");
			try {
				createStatement.execute();
			} catch (SQLException e) {
				logger.warn("Sequence has NOT been created.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
