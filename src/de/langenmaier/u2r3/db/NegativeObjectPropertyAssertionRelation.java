package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

public class NegativeObjectPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(NegativeObjectPropertyAssertionRelation.class);
	
	protected NegativeObjectPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "negativeObjectPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
			OWLNegativeObjectPropertyAssertionAxiom naxiom = (OWLNegativeObjectPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getIRI().toString());
			if (naxiom.getObject().isAnonymous()) {
				addStatement.setString(3, naxiom.getObject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(3, naxiom.getObject().asNamedIndividual().getIRI().toString());
			}
		}
		return AdditionMode.ADD;
	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void merge(DeltaRelation delta) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
