package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;

public class DataPropertyRangeRelation extends Relation {
	static Logger logger = Logger.getLogger(DataPropertyRangeRelation.class);
	
	protected DataPropertyRangeRelation() {
		try {
			tableName = "dataPropertyRange";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO dataPropertyRange (property, range) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLDataPropertyRangeAxiom naxiom = (OWLDataPropertyRangeAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLDataProperty().getURI().toString());
			addStatement.setString(2, naxiom.getRange().asOWLDatatype().getURI().toString());
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
