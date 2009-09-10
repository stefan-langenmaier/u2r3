package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;

public class DataPropertyDomainRelation extends Relation {
	static Logger logger = Logger.getLogger(DataPropertyDomainRelation.class);
	
	protected DataPropertyDomainRelation() {
		try {
			tableName = "dataPropertyDomain";
			
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
			OWLDataPropertyDomainAxiom naxiom = (OWLDataPropertyDomainAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLDataProperty().getURI().toString());
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
