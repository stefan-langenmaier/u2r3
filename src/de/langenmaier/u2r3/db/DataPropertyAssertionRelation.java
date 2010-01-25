package de.langenmaier.u2r3.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
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
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object, language, type) VALUES (?, ?, ?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom naxiom = (OWLDataPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			
			if (!naxiom.getObject().isTyped()) {
				addStatement.setString(3, naxiom.getObject().getLiteral());
				addStatement.setString(4, naxiom.getObject().asRDFTextLiteral().getLang());
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
		try {
			dropDelta(id);
			//max 3 Quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "(" +
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
					" CREATE HASH INDEX " + getDeltaName(id) + "_subject ON " + getDeltaName(id) + "(subject);" +
					" CREATE HASH INDEX " + getDeltaName(id) + "_property ON " + getDeltaName(id) + "(property);" +
					" CREATE HASH INDEX " + getDeltaName(id) + "_object ON " + getDeltaName(id) + "(object);");
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
						sql.append("SELECT id, '" + RelationName.dataPropertyAssertion + "' AS table,");
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

	@Override
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 3) {
			return "SELECT '1' FROM " + getTableName() + " WHERE subject = '" + args[0] + "' AND property = '" + args[1] + "' AND object ='" + args[2] + "'";
		} else if (args.length == 4) {
			return "SELECT '1' FROM " + getTableName() + " WHERE subject = '" + args[0] + "' AND property = '" + args[1] + "' AND object ='" + args[2] + "' AND (language = '" + args[3] + "' OR language IS NULL)";
		}
		throw new U2R3NotImplementedException();
	}

	public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual ni,
			OWLDataProperty dp) throws OWLReasonerException {
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
				//TODO Sprache oder Typ erzeugen
				if (type == null) {
					ret.add(dataFactory.getOWLStringLiteral(lit));
				} else {
					ret.add(dataFactory.getOWLTypedLiteral(lit));
				}
				
			}
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
