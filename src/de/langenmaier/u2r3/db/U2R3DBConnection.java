package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DatabaseMode;

/**
 * To use only one database connection a singleton connection is created.
 * @author stefan
 *
 */
public class U2R3DBConnection {
	private static U2R3DBConnection singleton;
	private Connection theDBConnection = null;
	
	private U2R3DBConnection(DatabaseMode databaseMode) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if (databaseMode == DatabaseMode.EMBEDDED) {
				theDBConnection = DriverManager.getConnection("jdbc:h2:~/u2r3", "sa", "");
			} else if (databaseMode == DatabaseMode.IN_MEMORY) {
				theDBConnection = DriverManager.getConnection("jdbc:h2:mem:", "sa", "");
			} else if (databaseMode == DatabaseMode.STANDALONE){
				theDBConnection = DriverManager.getConnection("jdbc:postgresql:u2r3", "u2r3", "u2r3");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Connection getConnection(){
		if (singleton == null) {
			Settings s = new Settings();
			singleton = new U2R3DBConnection(s.getDatabaseMode());
		}
		return singleton.theDBConnection;
		
	}

}
