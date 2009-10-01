package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Pair;

public class PropertyRangeRelation extends Relation {
	static Logger logger = Logger.getLogger(PropertyRangeRelation.class);
	
	protected PropertyRangeRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyRange";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, " +
					" property VARCHAR(100)," +
					" range VARCHAR(100)," +
					" PRIMARY KEY (property, range))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (property, range) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDataPropertyRangeAxiom) {
			OWLDataPropertyRangeAxiom naxiom = (OWLDataPropertyRangeAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLDataProperty().getURI().toString());
			addStatement.setString(2, naxiom.getRange().asOWLDatatype().getURI().toString());
		} else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
			OWLObjectPropertyRangeAxiom naxiom = (OWLObjectPropertyRangeAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			addStatement.setString(2, naxiom.getRange().asOWLClass().getURI().toString());
		}
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, " +
					" property VARCHAR(100)," +
					" range VARCHAR(100)," +
					" propertySourceId UUID, " +
					" propertySourceTable VARCHAR(100), " +
					" rangeSourceId UUID, " +
					" rangeSourceTable VARCHAR(100), " +
					" PRIMARY KEY (property, range))");
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
