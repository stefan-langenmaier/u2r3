package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class NegativeDataPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(NegativeDataPropertyAssertionRelation.class);
	
	protected NegativeDataPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "negativeDataPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" language TEXT NULL,"+
					" type TEXT NULL,"+
					" PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object, language, type) VALUES (?, ?, ?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom) {
			OWLNegativeDataPropertyAssertionAxiom naxiom = (OWLNegativeDataPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			addStatement.setString(3, naxiom.getObject().getLiteral());
			if (!naxiom.getObject().isTyped()) {
				addStatement.setString(4, naxiom.getObject().asRDFTextLiteral().getLang());
				addStatement.setNull(5, Types.LONGVARCHAR);
			} else {
				addStatement.setNull(4, Types.LONGVARCHAR);
				addStatement.setString(5, naxiom.getObject().asOWLStringLiteral().getDatatype().getIRI().toString());
			}
			
		}
		return AdditionMode.ADD;
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