package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

public class SubDataPropertyRelation extends Relation {
	protected static SubDataPropertyRelation theRelation;
	static Logger logger = Logger.getLogger(SubDataPropertyRelation.class);
	
	private SubDataPropertyRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE dataSubProperty (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropMainStatement = conn.prepareStatement("DROP TABLE dataSubProperty IF EXISTS ");
			createAuxStatement = conn.prepareStatement("CREATE TABLE dataSubPropertyAux (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropAuxStatement = conn.prepareStatement("DROP TABLE dataSubPropertyAux IF EXISTS ");
			createDeltaStatement = conn.prepareStatement("CREATE TABLE dataSubPropertyDelta (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropDeltaStatement = conn.prepareStatement("DROP TABLE dataSubPropertyDelta IF EXISTS ");
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO dataSubProperty (sub, super) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static SubDataPropertyRelation getRelation() {
		if (theRelation == null) theRelation = new SubDataPropertyRelation();
		return theRelation;
		
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLSubDataPropertyOfAxiom naxiom = (OWLSubDataPropertyOfAxiom) axiom;
			addStatement.setString(1, naxiom.getSubProperty().asOWLDataProperty().getURI().toString());
			addStatement.setString(2, naxiom.getSuperProperty().asOWLDataProperty().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
