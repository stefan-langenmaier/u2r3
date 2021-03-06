package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerException;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3NotQueryable;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClassAssertionEntRelation extends Relation {
	static Logger logger = Logger.getLogger(ClassAssertionEntRelation.class);
	
	protected ClassAssertionEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "classAssertionEnt";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" entity TEXT," +
					" colClass TEXT," +
					" PRIMARY KEY (entity, colClass));" +
					" CREATE INDEX " + tableName + "_entity ON " + tableName + "(entity);" +
					" CREATE INDEX " + tableName + "_class ON " + tableName + "(colClass);");
			
			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		//max 4 quellen außer in cls-int1
		return "CREATE TABLE " + table + "(" +
			" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
			" entity TEXT," +
			" colClass TEXT," +
			" sourceId1 BIGINT," +
			" sourceTable1 VARCHAR(100)," +
			" sourceId2 BIGINT," +
			" sourceTable2 VARCHAR(100)," +
			" sourceId3 BIGINT," +
			" sourceTable3 VARCHAR(100)," +
			" sourceId4 BIGINT," +
			" sourceTable4 VARCHAR(100)," +
			" PRIMARY KEY (id, entity, colClass));" +
			" CREATE INDEX " + table + "_entity ON " + table + "(entity);" +
			" CREATE INDEX " + table + "_class ON " + table + "(colClass);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (entity, colClass) VALUES (?, ?)";
	}

	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		PreparedStatement add = addStatement;

		for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
			if (axiom instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;
				add.setString(1, naxiom.getIndividual().asOWLNamedIndividual().getIRI().toString());
				add.setString(2, naxiom.getClassExpression().asOWLClass().getIRI().toURI().toString());
			} else if (axiom instanceof OWLAsymmetricObjectPropertyAxiom) {
				OWLAsymmetricObjectPropertyAxiom naxiom = (OWLAsymmetricObjectPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_ASYMMETRIC_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLSymmetricObjectPropertyAxiom) {
				OWLSymmetricObjectPropertyAxiom naxiom = (OWLSymmetricObjectPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_SYMMETRIC_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLFunctionalObjectPropertyAxiom) {
				OWLFunctionalObjectPropertyAxiom naxiom = (OWLFunctionalObjectPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_FUNCTIONAL_OBJECT_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLFunctionalDataPropertyAxiom) {
				OWLFunctionalDataPropertyAxiom naxiom = (OWLFunctionalDataPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_FUNCTIONAL_DATA_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) {
				OWLInverseFunctionalObjectPropertyAxiom naxiom = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_INVERSE_FUNCTIONAL_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLIrreflexiveObjectPropertyAxiom) {
				OWLIrreflexiveObjectPropertyAxiom naxiom = (OWLIrreflexiveObjectPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_IRREFLEXIVE_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLTransitiveObjectPropertyAxiom) {
				OWLTransitiveObjectPropertyAxiom naxiom = (OWLTransitiveObjectPropertyAxiom) axiom;
				add.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
				add.setString(2, OWLRDFVocabulary.OWL_TRANSITIVE_PROPERTY.getIRI().toString());
			} else if (axiom instanceof OWLDeclarationAxiom) {
				OWLDeclarationAxiom naxiom = (OWLDeclarationAxiom) axiom;
				add.setString(1, naxiom.getEntity().getIRI().toString());
				
				if (naxiom.getEntity().getEntityType() == EntityType.OBJECT_PROPERTY) {				
					add.setString(2, OWLRDFVocabulary.OWL_OBJECT_PROPERTY.getIRI().toString());
				} else if (naxiom.getEntity().getEntityType() == EntityType.DATA_PROPERTY) {				
					add.setString(2, OWLRDFVocabulary.OWL_DATA_PROPERTY.getIRI().toString());
				} else if (naxiom.getEntity().getEntityType() == EntityType.CLASS) {				
					add.setString(2, OWLRDFVocabulary.OWL_CLASS.getIRI().toString());
				} else if (naxiom.getEntity().getEntityType() == EntityType.NAMED_INDIVIDUAL) {				
					add.setString(2, OWLRDFVocabulary.OWL_NAMED_INDIVIDUAL.getIRI().toString());
				} else if (naxiom.getEntity().getEntityType() == EntityType.ANNOTATION_PROPERTY) {				
					add.setString(2, OWLRDFVocabulary.OWL_ANNOTATION_PROPERTY.getIRI().toString());
				} else {
					logger.error(naxiom.getEntity().getEntityType().toString());
					throw new U2R3NotImplementedException();
				}
			} else {
				throw new U2R3NotImplementedException();
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT entity, colClass FROM " + getTableName() + " AS bottom WHERE bottom.entity = t1.entity AND bottom.colClass = t1.colClass)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, entity, colClass) " +
					" SELECT MIN(id), entity, colClass " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY entity, colClass");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=4; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT b.theid, '" + RelationName.classAssertionEnt + "' AS colTable,");
						sql.append(" sourceId" + i + ", sourceTable" + i + "");
						sql.append("\n FROM " + delta.getDeltaName() + " AS t");
						sql.append("\n\t INNER JOIN (");
						sql.append("\n\t\t SELECT MIN(id) AS theid, entity, colClass ");
						sql.append("\n\t\t FROM " + delta.getDeltaName());
						sql.append("\n\t\t GROUP BY entity, colClass");
						sql.append("\n\t ) AS b ON b.entity = t.entity AND b.colClass = t.colClass");
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
		if (axiom instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;

			if (naxiom.getIndividual().isAnonymous()) {
				removeObject(naxiom.getIndividual());
			}
			
			if (naxiom.getClassExpression().isAnonymous()) {
				removeObject(naxiom.getClassExpression());
			}
			
		}
	}

	public NodeSet<OWLClass> getTypes(OWLNamedIndividual namedIndividual) {
		try {
			StringBuilder sql = new StringBuilder();
		
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			OWLClassNodeSet ret = new OWLClassNodeSet();
			
			
			sql.append("SELECT colClass");
			sql.append("\nFROM " + getTableName());
			sql.append("\nWHERE entity = '" + namedIndividual.getIRI().toString() + "'");
			
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()) {
				String iri = rs.getString("colClass");
				ret.addEntity(dataFactory.getOWLClass(IRI.create(iri)));
			}
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public NodeSet<OWLNamedIndividual> getIndividuals(OWLClass clazz) throws OWLReasonerException {
		try {
			StringBuilder sql = new StringBuilder();
		
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			OWLNamedIndividualNodeSet ret = new OWLNamedIndividualNodeSet();
			
			
			sql.append("SELECT entity");
			sql.append("\nFROM " + getTableName());
			sql.append("\nWHERE colClass = '" + clazz.getIRI().toString() + "'");
			
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()) {
				String iri = rs.getString("entity");
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
		if (ax instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom nax = (OWLClassAssertionAxiom) ax;
			String entity = null;
			String clazz = null;
			String tableId = TableId.getId();
			
			if (nax.getIndividual().isNamed()) {
				entity = nax.getIndividual().asOWLNamedIndividual().getIRI().toString();
			} else {
				throw new U2R3NotQueryable();
			}
			
			if (!nax.getClassExpression().isAnonymous()) {
				clazz = nax.getClassExpression().asOWLClass().getIRI().toString();
			}			
			
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			if (clazz != null) {
				sql.append("entity='" + entity + "' AND colClass='" + clazz + "'");
			} else {
				sql.append("entity='" + entity + "' ");
				sql.append(" AND EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getClassExpression(), tableId, "colClass");
			}
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}

}
