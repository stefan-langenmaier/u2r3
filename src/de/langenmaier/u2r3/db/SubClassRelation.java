package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;

public class SubClassRelation extends Relation {
//	protected static SubClassRelation theRelation;
	static Logger logger = Logger.getLogger(SubClassRelation.class);
	
	protected SubClassRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE subClass (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropMainStatement = conn.prepareStatement("DROP TABLE subClass IF EXISTS ");
//			createAuxStatement = conn.prepareStatement("CREATE TABLE subClassAux (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
//			dropAuxStatement = conn.prepareStatement("DROP TABLE subClassAux IF EXISTS ");
//			createDeltaStatement = conn.prepareStatement("CREATE TABLE subClassDelta (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
//			dropDeltaStatement = conn.prepareStatement("DROP TABLE subClassDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO subClass (sub, super) VALUES (?, ?)");
			
			tableName = "subClass";
			
			//add dependent rules
			rules.add(RuleManager.getRule(RuleManager.RuleName.transSubClass));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
//	public static SubClassRelation getRelation() {
//		if (theRelation == null) theRelation = new SubClassRelation();
//		return theRelation;
//		
//	}
	
	public void add(OWLAxiom axiom) {
		try {
			OWLSubClassOfAxiom naxiom = (OWLSubClassOfAxiom) axiom;
			addStatement.setString(1, naxiom.getSubClass().asOWLClass().getURI().toString());
			addStatement.setString(2, naxiom.getSuperClass().asOWLClass().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE subClass_d" + id + " (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE subClass_d" + id + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM subClass_d"+ delta.getDelta() + " AS t1 WHERE EXISTS (SELECT sub, super FROM SUBCLASS AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t1.super)");
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO subClass (sub, super) SELECT sub,  super FROM ( " +
					" SELECT sub, super " +
					" FROM subClass_d"+ delta.getDelta() + " " +
					")");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
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
