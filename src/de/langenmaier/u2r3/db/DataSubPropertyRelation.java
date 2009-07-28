package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;

public class DataSubPropertyRelation extends Relation {
	protected static DataSubPropertyRelation theRelation;
	static Logger logger = Logger.getLogger(DataSubPropertyRelation.class);
	
	private DataSubPropertyRelation() {
		try {
			createStatement = conn.prepareStatement("CREATE TABLE dataSubProperty (sub VARCHAR(100), super VARCHAR(100), PRIMARY KEY (sub, super))");
			dropStatement = conn.prepareStatement("DROP TABLE dataSubProperty IF EXISTS ");
			create();
			addStatement = conn.prepareStatement("INSERT INTO dataSubProperty (sub, super) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static DataSubPropertyRelation getRelation() {
		if (theRelation == null) theRelation = new DataSubPropertyRelation();
		return theRelation;
		
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLDataSubPropertyAxiom naxiom = (OWLDataSubPropertyAxiom) axiom;
			addStatement.setString(1, naxiom.getSubProperty().asOWLDataProperty().getURI().toString());
			addStatement.setString(2, naxiom.getSuperProperty().asOWLDataProperty().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
