package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import de.langenmaier.u2r3.rules.TransSubClassRule;

public class SubClassRelation extends Relation {
	protected static SubClassRelation theRelation;
	static Logger logger = Logger.getLogger(SubClassRelation.class);
	
	private SubClassRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE subClass (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropMainStatement = conn.prepareStatement("DROP TABLE subClass IF EXISTS ");
			createAuxStatement = conn.prepareStatement("CREATE TABLE subClassAux (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropAuxStatement = conn.prepareStatement("DROP TABLE subClassAux IF EXISTS ");
			createDeltaStatement = conn.prepareStatement("CREATE TABLE subClassDelta (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropDeltaStatement = conn.prepareStatement("DROP TABLE subClassDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO subClass (sub, super) VALUES (?, ?)");
			
			tableName = "subClass";
			
			//add dependant rules
			rules.add(new TransSubClassRule());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static SubClassRelation getRelation() {
		if (theRelation == null) theRelation = new SubClassRelation();
		return theRelation;
		
	}
	
	public void add(OWLAxiom axiom) {
		try {
			OWLSubClassAxiom naxiom = (OWLSubClassAxiom) axiom;
			addStatement.setString(1, naxiom.getSubClass().asOWLClass().getURI().toString());
			addStatement.setString(2, naxiom.getSuperClass().asOWLClass().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	


}
