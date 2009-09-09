package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;

public class DataPropertyRangeRelation extends Relation {
//	protected static DataPropertyRangeRelation theRelation;
	static Logger logger = Logger.getLogger(DataPropertyRangeRelation.class);
	
	protected DataPropertyRangeRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE dataPropertyRange (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
			dropMainStatement = conn.prepareStatement("DROP TABLE dataPropertyRange IF EXISTS ");
//			createAuxStatement = conn.prepareStatement("CREATE TABLE dataPropertyRangeAux (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
//			dropAuxStatement = conn.prepareStatement("DROP TABLE dataPropertyRangeAux IF EXISTS ");
//			createDeltaStatement = conn.prepareStatement("CREATE TABLE dataPropertyRangeDelta (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
//			dropDeltaStatement = conn.prepareStatement("DROP TABLE dataPropertyRangeDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO dataPropertyRange (property, range) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public static DataPropertyRangeRelation getRelation() {
//		if (theRelation == null) theRelation = new DataPropertyRangeRelation();
//		return theRelation;
//		
//	}
	
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
			createDeltaStatement.execute("CREATE TABLE dataPropertyRangeDelta_d" + id + " (property VARCHAR(100), domain VARCHAR(100), PRIMARY KEY (property, domain))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE dataPropertyRangeDelta_d" + id + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}

}
