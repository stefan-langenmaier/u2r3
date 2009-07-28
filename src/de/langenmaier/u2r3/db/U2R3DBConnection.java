package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * To use only one database connection a singleton connection is created.
 * @author stefan
 *
 */
public class U2R3DBConnection {
	private static U2R3DBConnection singleton;
	private Connection theDBConnection = null;
	
	private U2R3DBConnection() {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			theDBConnection = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "sa", "");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Connection getConnection(){
		if (singleton == null) singleton = new U2R3DBConnection();
		return singleton.theDBConnection;
		
	}

}
