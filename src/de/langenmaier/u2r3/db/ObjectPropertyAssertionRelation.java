package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

public class ObjectPropertyAssertionRelation extends Relation {
//	protected static ObjectPropertyAssertionRelation theRelation;
	static Logger logger = Logger.getLogger(ObjectPropertyAssertionRelation.class);
	
	protected ObjectPropertyAssertionRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE objectPropertyAssertion (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE objectPropertyAssertion IF EXISTS ");
//			createAuxStatement = conn.prepareStatement("CREATE TABLE objectPropertyAssertionAux (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
//			dropAuxStatement = conn.prepareStatement("DROP TABLE objectPropertyAssertionAux IF EXISTS ");
//			createDeltaStatement = conn.prepareStatement("CREATE TABLE objectPropertyAssertionDelta (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
//			dropDeltaStatement = conn.prepareStatement("DROP TABLE objectPropertyAssertionDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO objectPropertyAssertion (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public static ObjectPropertyAssertionRelation getRelation() {
//		if (theRelation == null) theRelation = new ObjectPropertyAssertionRelation();
//		return theRelation;
//		
//	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(3, naxiom.getObject().asNamedIndividual().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createDelta(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE objectPropertyAssertion_d" + id + " (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE objectPropertyAssertion_d" + id + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
