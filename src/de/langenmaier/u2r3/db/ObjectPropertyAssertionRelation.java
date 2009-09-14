package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Pair;

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
	public void addImpl(OWLAxiom axiom) throws SQLException {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(3, naxiom.getObject().asNamedIndividual().getURI().toString());
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
