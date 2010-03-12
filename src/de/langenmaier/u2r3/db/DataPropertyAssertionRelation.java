package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotQueryable;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.DatatypeCheck;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class DataPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(DataPropertyAssertionRelation.class);
	
	protected DataPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "dataPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" language TEXT NULL,"+
					" type TEXT NULL,"+
					" PRIMARY KEY (subject, property, object));" +
					" CREATE INDEX " + tableName + "_subject ON " + tableName + "(subject);" +
					" CREATE INDEX " + tableName + "_property ON " + tableName + "(property);" +
					" CREATE INDEX " + tableName + "_object ON " + tableName + "(object);");

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + "(" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" subject TEXT," +
		" property TEXT," +
		" object TEXT," +
		" language TEXT," +
		" type TEXT," +
		" sourceId1 BIGINT," +
		" sourceTable1 VARCHAR(100)," +
		" sourceId2 BIGINT," +
		" sourceTable2 VARCHAR(100)," +
		" sourceId3 BIGINT," +
		" sourceTable3 VARCHAR(100)," +
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
		if (axiom instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom naxiom = (OWLDataPropertyAssertionAxiom) axiom;
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
				if (naxiom.getSubject().isAnonymous()) {
					add.setString(1, naxiom.getSubject().asOWLAnonymousIndividual().toStringID());
				} else {
					add.setString(1, naxiom.getSubject().asOWLNamedIndividual().getIRI().toString());
				}
				add.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
				
				if (naxiom.getObject().isOWLTypedLiteral()) {
					add.setString(3, DatatypeCheck.validateType(naxiom.getObject().getLiteral(), naxiom.getObject().asOWLTypedLiteral().getDatatype()));
					add.setNull(4, Types.LONGVARCHAR);
					add.setString(5, naxiom.getObject().asOWLTypedLiteral().getDatatype().getIRI().toString());
				} else {
					add.setString(3, naxiom.getObject().getLiteral());
					add.setString(4, naxiom.getObject().getLang());
					add.setString(5, OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI().toString());
				}
			}
		}
		return AdditionMode.ADD;
	}

	@Override
	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT subject, property, object FROM " + getTableName() + " AS bottom WHERE bottom.subject = t1.subject AND bottom.property = t1.property AND bottom.object = t1.object)");
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, subject, property, object, language, type) " +
					" SELECT id, subject, property, object, language, type " +
					" FROM " + delta.getDeltaName() );

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=3; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT id, '" + RelationName.dataPropertyAssertion + "' AS colTable,");
						sql.append(" sourceId" + i + ", sourceTable" + i + "");
						sql.append("\n FROM " + delta.getDeltaName() + " AS t");
						sql.append("\n WHERE sourceId" + i + " IS NOT NULL");
						
						relationManager.addHistory(sql.toString());
					}
				}
				
				//fire reason
				logger.debug("Relation (" + toString()  + ") has got new data");
				Reason r = new AdditionReason(this, delta);
				reasonProcessor.add(r);
			}
			
			isDirty = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ni,
			OWLDataProperty dp) {
		try {
			StringBuilder sql = new StringBuilder();
		
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			Set<OWLLiteral> ret = new HashSet<OWLLiteral>();
			
			
			sql.append("SELECT object, language, type");
			sql.append("\nFROM " + getTableName());
			sql.append("\nWHERE subject = '" + ni.getIRI().toString() + "' AND property = '" + dp.getIRI().toString() + "'");
			
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()) {
				String lit = rs.getString("object");
				String type = rs.getString("type");
				if (type == null) {
					String language = rs.getString("language");
					ret.add(dataFactory.getOWLStringLiteral(lit,language));
				} else {
					ret.add(dataFactory.getOWLTypedLiteral(lit,dataFactory.getOWLDatatype(IRI.create(type))));
				}
				
			}
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom nax = (OWLDataPropertyAssertionAxiom) ax;
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
