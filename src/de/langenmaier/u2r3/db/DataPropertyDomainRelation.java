package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;

public class DataPropertyDomainRelation extends Relation {
	protected static DataPropertyDomainRelation theRelation;
	static Logger logger = Logger.getLogger(DataPropertyDomainRelation.class);
	
	private DataPropertyDomainRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE dataPropertyDomain (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropMainStatement = conn.prepareStatement("DROP TABLE dataPropertyDomain IF EXISTS ");
			createAuxStatement = conn.prepareStatement("CREATE TABLE dataPropertyDomainAux (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropAuxStatement = conn.prepareStatement("DROP TABLE dataPropertyDomainAux IF EXISTS ");
			createDeltaStatement = conn.prepareStatement("CREATE TABLE dataPropertyDomainDelta (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropDeltaStatement = conn.prepareStatement("DROP TABLE dataPropertyDomainDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO dataPropertyDomain (property, domain) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static DataPropertyDomainRelation getRelation() {
		if (theRelation == null) theRelation = new DataPropertyDomainRelation();
		return theRelation;
		
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLDataPropertyDomainAxiom naxiom = (OWLDataPropertyDomainAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLDataProperty().getURI().toString());
			addStatement.setString(2, naxiom.getDomain().asOWLClass().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
