package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;

public class ObjectPropertyRangeRelation extends Relation {
	protected static ObjectPropertyRangeRelation theRelation;
	static Logger logger = Logger.getLogger(ObjectPropertyRangeRelation.class);
	
	private ObjectPropertyRangeRelation() {
		try {
			createStatement = conn.prepareStatement("CREATE TABLE objectPropertyRange (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
			dropStatement = conn.prepareStatement("DROP TABLE objectPropertyRange IF EXISTS ");
			create();
			addStatement = conn.prepareStatement("INSERT INTO objectPropertyRange (property, range) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ObjectPropertyRangeRelation getRelation() {
		if (theRelation == null) theRelation = new ObjectPropertyRangeRelation();
		return theRelation;
		
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLObjectPropertyRangeAxiom naxiom = (OWLObjectPropertyRangeAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(2, naxiom.getRange().asOWLClass().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
