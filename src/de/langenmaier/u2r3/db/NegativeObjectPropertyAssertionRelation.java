package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3NotQueryable;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.TableId;

public class NegativeObjectPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(NegativeObjectPropertyAssertionRelation.class);
	
	protected NegativeObjectPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "negativeObjectPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" PRIMARY KEY (subject, property, object));" +
					" CREATE INDEX " + getTableName() + "_subject ON " + getTableName() + "(subject);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_object ON " + getTableName() + "(object);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
			OWLNegativeObjectPropertyAssertionAxiom naxiom = (OWLNegativeObjectPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asOWLAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asOWLNamedIndividual().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getIRI().toString());
			if (naxiom.getObject().isAnonymous()) {
				addStatement.setString(3, naxiom.getObject().asOWLAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(3, naxiom.getObject().asOWLNamedIndividual().getIRI().toString());
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
		if (ax instanceof OWLNegativeObjectPropertyAssertionAxiom) {
			OWLNegativeObjectPropertyAssertionAxiom nax = (OWLNegativeObjectPropertyAssertionAxiom) ax;
			String subject = null;
			String property = null;
			String object = null;
			String tableId = TableId.getId();
			
			if (nax.getSubject().isNamed()) {
				subject = nax.getSubject().asOWLNamedIndividual().getIRI().toString();
			} else {
				throw new U2R3NotQueryable();
			}
			
			if (!nax.getProperty().isAnonymous()) {
				property = nax.getProperty().asOWLObjectProperty().getIRI().toString();
			}			
			
			if (nax.getObject().isNamed()) {
				object = nax.getObject().asOWLNamedIndividual().getIRI().toString();
			} else {
				throw new U2R3NotQueryable();
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT uid, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			if (property != null) {
				sql.append("subject='" + subject + "' AND property='" + property + "' AND object='" + object + "'");
			} else {
				sql.append("subject='" + subject + "' ");
				sql.append(" AND EXISTS "); //property
				handleSubAxiomLocationImpl(sql, nax.getProperty(), tableId, "property");
				sql.append(" AND object='" + object + "'");
			}
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}

}
