package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

public class ObjectPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(ObjectPropertyAssertionRelation.class);
	
	protected ObjectPropertyAssertionRelation() {
		try {
			tableName = "objectPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
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
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (subject VARCHAR(100), property VARCHAR(100), object VARCHAR(100), PRIMARY KEY (subject, property, object))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE " + getDeltaName(id) + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}

}
