package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

public class ClassAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(ClassAssertionRelation.class);
	
	protected ClassAssertionRelation() {
		try {
			tableName = "classAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");
			
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, type) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
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
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
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
