package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class PropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(PropertyAssertionRelation.class);
	
	protected PropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom naxiom = (OWLDataPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getURI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getURI().toString());
			if (naxiom.getObject().isTyped()) {
				/*for (OWLDatatype dt : naxiom.getObject().getDatatypesInSignature()) {
					//TODO add für classassertion aufrufen mit dt und literal
					OWLDataFactory df;// = new OWLDataFactory();
					//df.
				}*/
			}
			addStatement.setString(3, naxiom.getObject().getLiteral());
		} else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getURI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			if (naxiom.getObject().isAnonymous()) {
				addStatement.setString(3, naxiom.getObject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(3, naxiom.getObject().asNamedIndividual().getURI().toString());
			}
		}
		return true;
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "" +
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
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

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
