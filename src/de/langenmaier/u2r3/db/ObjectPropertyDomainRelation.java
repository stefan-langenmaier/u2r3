package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;

public class ObjectPropertyDomainRelation extends Relation {
	static Logger logger = Logger.getLogger(ObjectPropertyDomainRelation.class);
	
	protected ObjectPropertyDomainRelation() {
		try {
			tableName = "objectPropertyDomain";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (property, domain) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
			OWLObjectPropertyDomainAxiom naxiom = (OWLObjectPropertyDomainAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(2, naxiom.getDomain().asOWLClass().getURI().toString());
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}

}
