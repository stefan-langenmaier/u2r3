package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class AssertionPropertyRelation extends Relation {
	
	protected AssertionPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "assertionProperty";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" name TEXT," +
					" property TEXT," +
					" PRIMARY KEY (name, property))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (name, property) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
			OWLNegativeObjectPropertyAssertionAxiom naxiom = (OWLNegativeObjectPropertyAssertionAxiom) axiom;
			addStatement.setString(1, nidMapper.get(naxiom).toString());
			if (naxiom.getProperty().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getProperty()).toString());
				handleAnonymousObjectPropertyExpression(naxiom.getProperty());
			} else {
				addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getIRI().toString());
			}
			return AdditionMode.ADD;
		} else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom) {
			OWLNegativeDataPropertyAssertionAxiom naxiom = (OWLNegativeDataPropertyAssertionAxiom) axiom;
			addStatement.setString(1, nidMapper.get(naxiom).toString());
			if (naxiom.getProperty().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getProperty()).toString());
				handleAnonymousDataPropertyExpression(naxiom.getProperty());
			} else {
				addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			}
			return AdditionMode.ADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void merge(DeltaRelation delta) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
