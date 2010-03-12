package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotQueryable;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.DatatypeCheck;

public class NegativeDataPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(NegativeDataPropertyAssertionRelation.class);
	
	protected NegativeDataPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "negativeDataPropertyAssertion";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" subject TEXT," +
		" property TEXT," +
		" object TEXT," +
		" language TEXT NULL,"+
		" type TEXT NULL,"+
		" PRIMARY KEY (subject, property, object));" +
		" CREATE INDEX " + table + "_subject ON " + table + "(subject);" +
		" CREATE INDEX " + table + "_property ON " + table + "(property);" +
		" CREATE INDEX " + table + "_object ON " + table + "(object);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (subject, property, object, language, type) VALUES (?, ?, ?, ?, ?)";
	}

	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom) {
			OWLNegativeDataPropertyAssertionAxiom naxiom = (OWLNegativeDataPropertyAssertionAxiom) axiom;
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
				if (naxiom.getSubject().isAnonymous()) {
					add.setString(1, naxiom.getSubject().asOWLAnonymousIndividual().toStringID());
				} else {
					add.setString(1, naxiom.getSubject().asOWLNamedIndividual().getIRI().toString());
				}
				add.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
				
				if (!naxiom.getObject().isOWLTypedLiteral()) {
					add.setString(3, naxiom.getObject().getLiteral());
					add.setString(4, naxiom.getObject().getLang());
					add.setString(5, OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI().toString());
				} else {
					add.setString(3, DatatypeCheck.validateType(naxiom.getObject().getLiteral(), naxiom.getObject().asOWLStringLiteral().getDatatype()));
					add.setNull(4, Types.LONGVARCHAR);
					add.setString(5, naxiom.getObject().asOWLStringLiteral().getDatatype().getIRI().toString());
				}
			}
		}
		return AdditionMode.ADD;
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
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
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