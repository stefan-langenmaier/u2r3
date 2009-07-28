package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;

public class ObjectPropertyAssertionRelation extends Relation {
	protected static ObjectPropertyAssertionRelation theRelation;
	static Logger logger = Logger.getLogger(ObjectPropertyAssertionRelation.class);
	
	private ObjectPropertyAssertionRelation() {
		try {
			createStatement = conn.prepareStatement("CREATE TABLE objectPropertyAssertion (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
			dropStatement = conn.prepareStatement("DROP TABLE objectPropertyAssertion IF EXISTS ");
			create();
			addStatement = conn.prepareStatement("INSERT INTO objectPropertyAssertion (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ObjectPropertyAssertionRelation getRelation() {
		if (theRelation == null) theRelation = new ObjectPropertyAssertionRelation();
		return theRelation;
		
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getSubject().asOWLIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(3, naxiom.getObject().asOWLIndividual().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
