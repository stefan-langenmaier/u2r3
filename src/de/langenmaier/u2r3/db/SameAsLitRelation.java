package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SameAsLitRelation extends Relation {
	
	protected SameAsLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "sameAsLit";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left TEXT," +
					" right TEXT," +
					" left_language TEXT," +
					" left_type TEXT," +
					" right_language TEXT," +
					" right_type TEXT," +
					" PRIMARY KEY (id, left, right))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right, left_language, left_type, right_language, right_type) VALUES (?, ?, ?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			// bis zu 8 Quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left TEXT," +
					" right TEXT," +
					" left_language TEXT," +
					" left_type TEXT," +
					" right_language TEXT," +
					" right_type TEXT," +
					" sourceId1 UUID," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 UUID," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 UUID," +
					" sourceTable3 VARCHAR(100)," +
					" sourceId4 UUID," +
					" sourceTable4 VARCHAR(100)," +
					" sourceId5 UUID," +
					" sourceTable5 VARCHAR(100)," +
					" sourceId6 UUID," +
					" sourceTable6 VARCHAR(100)," +
					" PRIMARY KEY (id, left, right))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT left, right FROM " + getTableName() + " AS bottom WHERE bottom.left = t1.left AND bottom.right = t1.right)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, left, right) " +
					" SELECT MIN(id), left, right " +
					" FROM " + delta.getDeltaName() + " " +
					" GROUP BY left, right");			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					for (int i=1; i<=6; ++i) {
						//remove rows without history
						sql = "DELETE FROM " + delta.getDeltaName() + " WHERE sourceId" + i + " IS NULL";
						rows = stmt.executeUpdate(sql);				
						
						//source
						sql = "SELECT id, '" + RelationName.sameAsLit + "' AS table, sourceId" + i + ", sourceTable" + i + " FROM " + delta.getDeltaName();
						relationManager.addHistory(sql);
					}
				}
				
				//fire reason
				logger.debug("Relation (" + toString()  + ") has got new data");
				Reason r = new AdditionReason(this, delta);
				reasonProcessor.add(r);
			}
			
			isDirty = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 1) {
			return "SELECT left FROM " + getTableName() + " WHERE left = '" + args[0] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
