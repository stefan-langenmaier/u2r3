package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.DeletionReason;
import de.langenmaier.u2r3.util.U2R3Component;

/**
 * This class stores how facts have been inferred and manages in which order facts can be deleted.
 * @author stefan
 *
 */
public class History extends U2R3Component {
	static Logger logger = Logger.getLogger(History.class);
	
	private Connection conn = null;
	private PreparedStatement createStatement = null;
	private PreparedStatement dropStatement = null;
	private Statement stmt = null;
	
	protected History(U2R3Reasoner reasoner) {
		super(reasoner);
		conn = U2R3DBConnection.getConnection();
		try {
			if (settings.startClean()) {
				dropStatement = conn.prepareStatement("DROP TABLE " + getTableName());
				try {
					dropStatement.execute();
				} catch (SQLException e) {
					logger.warn("Relation has NOT been deleted.");
				}
				createStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
						" id BIGINT NOT NULL," +
						" colTable VARCHAR(100) NOT NULL," +
						" sourceId BIGINT NOT NULL," +
						" sourceTable VARCHAR(100) NOT NULL," +
						" PRIMARY KEY (id, sourceId))");
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
			//TODO MERGE INTO sollte eigentlich INSERT INTO sein
			stmt.execute("MERGE INTO history (id, colTable, sourceId, sourceTable) " + sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.trace("Added history data from (" + sql + ")");
		
	}

	public void remove(Long sourceId, RelationName sourceTable) {
		logger.trace(" removing UID: "+ sourceId.toString());
		
		Statement stmt = null;
		Statement deleteStatement = null;
		ResultSet rs = null;
		String sql;
		sql = "SELECT id, colTable FROM " + getTableName() + " WHERE sourceId = '" + sourceId.toString() + "'";
		try {
			stmt = conn.createStatement();
			deleteStatement = conn.createStatement();
			
			//find dependencies
			rs = stmt.executeQuery(sql);
			
			//remove dependecies
			while (rs.next()) {
				Long id = rs.getLong("id");
				RelationName name = RelationName.valueOf(rs.getString("colTable"));
				remove(id, name);
				
				//remove history
				logger.trace(" remove history: "+ id.toString());
				sql = "DELETE FROM " + getTableName() + " WHERE id = '" + id.toString() + "'";
				deleteStatement.execute(sql);
				
				//fire reason - maybe data can be created again for this relation
				reasonProcessor.add(new DeletionReason(relationManager.getRelation(name)));
			}	
			
			//remove value
			logger.trace(" remove value: "+ sourceId.toString() + " von " + sourceTable);
			sql = "DELETE FROM " + relationManager.getRelation(sourceTable).getTableName() + " WHERE id = '" + sourceId.toString() + "'";
			deleteStatement.execute(sql);
		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		logger.trace(" removed UID: "+ sourceId.toString());
	}
}
