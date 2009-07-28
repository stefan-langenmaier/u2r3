package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;

public class ClassAssertionRelation extends Relation {
	protected static ClassAssertionRelation theRelation;
	static Logger logger = Logger.getLogger(ClassAssertionRelation.class);
	
	private ClassAssertionRelation() {
		try {
			createStatement = conn.prepareStatement("CREATE TABLE classAssertion (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
			dropStatement = conn.prepareStatement("DROP TABLE classAssertion IF EXISTS ");
			create();
			addStatement = conn.prepareStatement("INSERT INTO classAssertion (class, type) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ClassAssertionRelation getRelation() {
		if (theRelation == null) theRelation = new ClassAssertionRelation();
		return theRelation;
		
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getDescription().asOWLClass().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
