package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3NotQueryable;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.DatatypeCheck;

public class NegativeDataPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(NegativeDataPropertyAssertionRelation.class);
	
	protected NegativeDataPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "negativeDataPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" language TEXT NULL,"+
					" type TEXT NULL,"+
					" PRIMARY KEY (subject, property, object));" +
					" CREATE INDEX " + getTableName() + "_subject ON " + getTableName() + "(subject);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_object ON " + getTableName() + "(object);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

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
				addStatement.setString(1, naxiom.getSubject().asOWLAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asOWLNamedIndividual().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			
			if (!naxiom.getObject().isOWLTypedLiteral()) {
				addStatement.setString(3, naxiom.getObject().getLiteral());
				addStatement.setString(4, naxiom.getObject().getLang());
				addStatement.setString(5, OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI().toString());
			} else {
				addStatement.setString(3, DatatypeCheck.validateType(naxiom.getObject().getLiteral(), naxiom.getObject().asOWLStringLiteral().getDatatype()));
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
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}
	
	@Override
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLNegativeDataPropertyAssertionAxiom) {
			OWLNegativeDataPropertyAssertionAxiom nax = (OWLNegativeDataPropertyAssertionAxiom) ax;
			String subject = null;
			String property = null;
			String object = null;
			String language = null;
			String type = null;
			
			if (nax.getSubject().isNamed()) {
				subject = nax.getSubject().asOWLNamedIndividual().getIRI().toString();
			} else {
				throw new U2R3NotQueryable();
			}
			
			property = nax.getProperty().asOWLDataProperty().getIRI().toString();
			
			object = nax.getObject().getLiteral();
			
			if (nax.getObject().isOWLStringLiteral()) {
				language = nax.getObject().asOWLStringLiteral().getLang();
			} else {
				type = nax.getObject().asOWLTypedLiteral().getDatatype().asOWLDatatype().getIRI().toString();
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT uid, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName());
			sql.append("\nWHERE ");
			if (language == null) {
				sql.append("subject='" + subject + "' AND property='" + property + "' AND object='" + object + "' AND type='" + type + "'");
			} else {
				sql.append("subject='" + subject + "' AND property='" + property + "' AND object='" + object + "' AND language='" + language + "'");
			}
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}

}
