package de.langenmaier.u2r3.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ObjectPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(ObjectPropertyAssertionRelation.class);
	
	protected ObjectPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "objectPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" PRIMARY KEY (subject, property, object));" +
					" CREATE INDEX " + tableName + "_subject ON " + tableName + "(subject);" +
					" CREATE INDEX " + tableName + "_property ON " + tableName + "(property);" +
					" CREATE INDEX " + tableName + "_object ON " + tableName + "(object);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
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
		try {
			dropDelta(id);
			//max 3 Quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "(" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" sourceId1 BIGINT," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 BIGINT," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 BIGINT," +
					" sourceTable3 VARCHAR(100)," +
					" PRIMARY KEY HASH (id, subject, property, object));" +
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
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, subject, property, object) " +
					" SELECT MIN(id), subject, property, object " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY subject, property, object");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=3; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT b.theid, '" + RelationName.objectPropertyAssertion + "' AS table,");
						sql.append(" sourceId" + i + ", sourceTable" + i + "");
						sql.append("\n FROM " + delta.getDeltaName() + " AS t");
						sql.append("\n\t INNER JOIN (");
						sql.append("\n\t\t SELECT MIN(id) AS theid, subject, property, object ");
						sql.append("\n\t\t FROM " + delta.getDeltaName());
						sql.append("\n\t\t GROUP BY subject, property, object");
						sql.append("\n\t ) AS b ON b.subject = t.subject AND b.property = t.property AND b.object = t.object");
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
		if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			
			String tid = TableId.getId();
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id");
			sql.append("\nFROM " + getTableName() + " AS " + tid);
			sql.append("\nWHERE EXISTS (");
			getSubSQL(sql, naxiom.getSubject(), tid, "subject");
			sql.append(") AND EXISTS (");
			getSubSQL(sql, naxiom.getProperty(), tid, "property");
			sql.append(") AND EXISTS (");
			getSubSQL(sql, naxiom.getObject(), tid, "object");
			sql.append(")");
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			
			if (rs.next()) {
				relationManager.remove(rs.getLong("id"), RelationName.objectPropertyAssertion);
				
				if (naxiom.getSubject().isAnonymous()) {
					removeAnonymousIndividual(naxiom.getSubject());
				}
				
				if (naxiom.getProperty().isAnonymous()) {
					removeAnonymousPropertyExpression(naxiom.getProperty());
				}
				
				if (naxiom.getObject().isAnonymous()) {
					removeAnonymousIndividual(naxiom.getObject());
				}
				
			}
		} else {
			throw new U2R3NotImplementedException();
		}
	}


	@Override
	protected String existsImpl(String... args) {
		if (args.length == 3) {
			return "SELECT '1' FROM " + getTableName() + " WHERE subject = '" + args[0] + "' AND property = '" + args[1] + "' AND object ='" + args[2] + "'";
		}
		throw new U2R3NotImplementedException();
	}

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ni,
			OWLObjectProperty op) {
		try {
			StringBuilder sql = new StringBuilder();
		
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			OWLNamedIndividualNodeSet ret = new OWLNamedIndividualNodeSet();
			
			
			sql.append("SELECT object");
			sql.append("\nFROM " + getTableName());
			sql.append("\nWHERE subject = '" + ni.getIRI().toString() + "' AND property = '" + op.getIRI().toString() + "'");
			
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()) {
				String iri = rs.getString("object");
				ret.addEntity(dataFactory.getOWLNamedIndividual(IRI.create(iri)));
			}
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
