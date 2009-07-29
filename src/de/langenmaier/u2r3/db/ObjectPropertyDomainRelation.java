package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;

//XXX Sollte man vielleicht mit DataPropertyDomainRelation zusammenlegen
public class ObjectPropertyDomainRelation extends Relation {
	protected static ObjectPropertyDomainRelation theRelation;
	static Logger logger = Logger.getLogger(ObjectPropertyDomainRelation.class);
	
	private ObjectPropertyDomainRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE objectPropertyDomain (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropMainStatement = conn.prepareStatement("DROP TABLE objectPropertyDomain IF EXISTS ");
			createAuxStatement = conn.prepareStatement("CREATE TABLE objectPropertyDomainAux (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropAuxStatement = conn.prepareStatement("DROP TABLE objectPropertyDomainAux IF EXISTS ");
			createDeltaStatement = conn.prepareStatement("CREATE TABLE objectPropertyDomainDelta (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
			dropDeltaStatement = conn.prepareStatement("DROP TABLE objectPropertyDomainDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO objectPropertyDomain (property, domain) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ObjectPropertyDomainRelation getRelation() {
		if (theRelation == null) theRelation = new ObjectPropertyDomainRelation();
		return theRelation;
		
	}
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLObjectPropertyDomainAxiom naxiom = (OWLObjectPropertyDomainAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(2, naxiom.getDomain().asOWLClass().getURI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
