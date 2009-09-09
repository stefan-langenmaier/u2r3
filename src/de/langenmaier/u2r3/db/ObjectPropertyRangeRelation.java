package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;

public class ObjectPropertyRangeRelation extends Relation {
//	protected static ObjectPropertyRangeRelation theRelation;
	static Logger logger = Logger.getLogger(ObjectPropertyRangeRelation.class);
	
	protected ObjectPropertyRangeRelation() {
		try {
			createMainStatement = conn.prepareStatement("CREATE TABLE objectPropertyRange (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
			dropMainStatement = conn.prepareStatement("DROP TABLE objectPropertyRange IF EXISTS ");
//			createAuxStatement = conn.prepareStatement("CREATE TABLE objectPropertyRangeAux (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
//			dropAuxStatement = conn.prepareStatement("DROP TABLE objectPropertyRangeAux IF EXISTS ");
//			createDeltaStatement = conn.prepareStatement("CREATE TABLE objectPropertyRangeDelta (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
//			dropDeltaStatement = conn.prepareStatement("DROP TABLE objectPropertyRangeDelta IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO objectPropertyRange (property, range) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public static ObjectPropertyRangeRelation getRelation() {
//		if (theRelation == null) theRelation = new ObjectPropertyRangeRelation();
//		return theRelation;
//		
//	}
	
	@Override
	public void add(OWLAxiom axiom) {
		try {
			OWLObjectPropertyRangeAxiom naxiom = (OWLObjectPropertyRangeAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(2, naxiom.getRange().asOWLClass().getURI().toString());
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
			createDeltaStatement.execute("CREATE TABLE objectPropertyRange_d" + id + " (property VARCHAR(100), range VARCHAR(100), PRIMARY KEY (property, range))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dropDelta(long id) {
		try {
			dropDeltaStatement.execute("DROP TABLE objectPropertyRange_d" + id + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}

}
