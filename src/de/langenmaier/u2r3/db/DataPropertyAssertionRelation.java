package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;

import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Pair;

public class DataPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(DataPropertyAssertionRelation.class);
	
	protected DataPropertyAssertionRelation() {
		try {
			tableName = "dataPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject VARCHAR(100)," +
					" property VARCHAR(100)," +
					" object VARCHAR(100)," +
					" PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
			OWLDataPropertyAssertionAxiom naxiom = (OWLDataPropertyAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getURI().toString());
			addStatement.setString(3, naxiom.getObject().getLiteral());
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "" +
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject VARCHAR(100)," +
					" property VARCHAR(100)," +
					" object VARCHAR(100)," +
					" subjectSourceId UUID, " +
					" subjectSourceTable VARCHAR(100), " +
					" propertySourceId UUID, " +
					" propertySourceTable VARCHAR(100), " +
					" objectSourceId UUID, " +
					" objectSourceTable VARCHAR(100), " +
					" PRIMARY KEY (subject, property, object))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}