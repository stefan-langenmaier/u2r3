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

public class SourceIndividualRelation extends Relation {
	
	protected SourceIndividualRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "sourceIndividual";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" name TEXT," +
					" subject TEXT," +
					" PRIMARY KEY (name, subject))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (name, subject) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
			OWLNegativeObjectPropertyAssertionAxiom naxiom = (OWLNegativeObjectPropertyAssertionAxiom) axiom;
			addStatement.setString(1, nidMapper.get(naxiom).toString());
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(2, naxiom.getSubject().asAnonymousIndividual().getID().toString());
			} else {
				addStatement.setString(2, naxiom.getSubject().asNamedIndividual().getIRI().toString());
			}
			return AdditionMode.ADD;
		} else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom) {
			OWLNegativeDataPropertyAssertionAxiom naxiom = (OWLNegativeDataPropertyAssertionAxiom) axiom;
			addStatement.setString(1, nidMapper.get(naxiom).toString());
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(2, naxiom.getSubject().asAnonymousIndividual().getID().toString());
			} else {
				addStatement.setString(2, naxiom.getSubject().asNamedIndividual().getIRI().toString());
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
