package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SubClassRelation extends Relation {
	static Logger logger = Logger.getLogger(SubClassRelation.class);
	
	protected SubClassRelation() {
		try {
			tableName = "subClass";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO subClass (sub, super) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addImpl(OWLAxiom axiom) throws SQLException {
			OWLSubClassOfAxiom naxiom = (OWLSubClassOfAxiom) axiom;
			addStatement.setString(1, naxiom.getSubClass().asOWLClass().getURI().toString());
			addStatement.setString(2, naxiom.getSuperClass().asOWLClass().getURI().toString());
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, sub VARCHAR(100), super VARCHAR(100), subSourceId UUID NOT NULL, superSourceId UUID NOT NULL, PRIMARY KEY (sub, super))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + getDeltaName(delta.getDelta()) + " AS t1 WHERE EXISTS (SELECT sub, super FROM " + getTableName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t1.super)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, sub, super) SELECT id, sub,  super FROM ( " +
					" SELECT id, sub, super " +
					" FROM " + getDeltaName(delta.getDelta()) + " " +
					")");

			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (Settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					//subSource
					sql = "SELECT id, " + RelationName.subClass.ordinal() + " AS table, subSourceId, " + RelationName.subClass.ordinal() + " AS sourceTable FROM " + getDeltaName(delta.getDelta());
					RelationManager.addHistory(sql);
					
					//superSource
					sql = "SELECT id, " + RelationName.subClass.ordinal() + " AS table, superSourceId, " + RelationName.subClass.ordinal() + " AS sourceTable FROM " + getDeltaName(delta.getDelta());
					RelationManager.addHistory(sql);
				}
				
				//fire reason
				logger.debug("Relation (" + toString()  + ") has got new data");
				Reason r = new Reason(RelationManager.getRelation(RelationName.subClass), delta);
				ReasonProcessor.getReasonProcessor().add(r);
			}
			
			isDirty = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


}
