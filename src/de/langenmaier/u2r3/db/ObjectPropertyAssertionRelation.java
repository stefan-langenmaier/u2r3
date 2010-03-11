package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
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
import de.langenmaier.u2r3.exceptions.U2R3NotQueryable;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
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
			" sourceId1 BIGINT," +
			" sourceTable1 VARCHAR(100)," +
			" sourceId2 BIGINT," +
			" sourceTable2 VARCHAR(100)," +
			" sourceId3 BIGINT," +
			" sourceTable3 VARCHAR(100)," +
			" PRIMARY KEY (id, subject, property, object));" +
			" CREATE INDEX " + table + "_subject ON " + table + "(subject);" +
			" CREATE INDEX " + table + "_property ON " + table + "(property);" +
			" CREATE INDEX " + table + "_object ON " + table + "(object);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)";
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
				if (naxiom.getSubject().isAnonymous()) {
					add.setString(1, naxiom.getSubject().asOWLAnonymousIndividual().toStringID());
				} else {
					add.setString(1, naxiom.getSubject().asOWLNamedIndividual().getIRI().toString());
				}
				add.setString(2, naxiom.getProperty().asOWLObjectProperty().getIRI().toString());
				if (naxiom.getObject().isAnonymous()) {
					add.setString(3, naxiom.getObject().asOWLAnonymousIndividual().toStringID());
				} else {
					add.setString(3, naxiom.getObject().asOWLNamedIndividual().getIRI().toString());
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
						sql.append("SELECT b.theid, '" + RelationName.objectPropertyAssertion + "' AS colTable,");
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

			if (naxiom.getSubject().isAnonymous()) {
				removeObject(naxiom.getSubject());
			}
			
			if (naxiom.getProperty().isAnonymous()) {
				removeObject(naxiom.getProperty());
			}
			
			if (naxiom.getObject().isAnonymous()) {
				removeObject(naxiom.getObject());
			}
			
		
		} else {
			throw new U2R3NotImplementedException();
		}
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
	
	@Override
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom nax = (OWLObjectPropertyAssertionAxiom) ax;
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
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
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
