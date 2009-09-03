package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

public class ClassAssertionRelation extends Relation {
	//protected static ClassAssertionRelation theRelation;
	static Logger logger = Logger.getLogger(ClassAssertionRelation.class);
	
	protected ClassAssertionRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE classAssertion (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
			dropMainStatement = conn.prepareStatement("DROP TABLE classAssertion IF EXISTS ");
//			createAuxStatement = conn.prepareStatement("CREATE TABLE classAssertionAux (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
//			dropAuxStatement = conn.prepareStatement("DROP TABLE classAssertionAux IF EXISTS ");
//			createDeltaStatement = conn.prepareStatement("CREATE TABLE classAssertionDelta (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
//			dropDeltaStatement = conn.prepareStatement("DROP TABLE classAssertionDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO classAssertion (class, type) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

/*	public static ClassAssertionRelation getRelation() {
		if (theRelation == null) theRelation = new ClassAssertionRelation();
		return theRelation;
		
	}*/
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getIndividual().asNamedIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getClassExpression().asOWLClass().getIRI().toURI().toString());
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
			createDeltaStatement.execute("CREATE TABLE classAssertion_d" + id + " (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE classAssertion_d" + id + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
