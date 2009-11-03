package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClassAssertionEntRelation extends Relation {
	static Logger logger = Logger.getLogger(ClassAssertionEntRelation.class);
	
	protected ClassAssertionEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "classAssertionEnt";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, entity TEXT, class TEXT, PRIMARY KEY (entity, class))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (entity, class) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getIndividual().asNamedIndividual().getIRI().toString());
			addStatement.setString(2, naxiom.getClassExpression().asOWLClass().getIRI().toURI().toString());
			return true;
		} else if (axiom instanceof OWLAsymmetricObjectPropertyAxiom) {
			OWLAsymmetricObjectPropertyAxiom naxiom = (OWLAsymmetricObjectPropertyAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().getNamedProperty().getIRI().toString());
			addStatement.setString(2, OWLRDFVocabulary.OWL_ASYMMETRIC_PROPERTY.getIRI().toString());
			return true;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			//max 4 quellen außer in cls-int1
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + 
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" entity TEXT," +
					" class TEXT," +
					" sourceId1 UUID," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 UUID," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 UUID," +
					" sourceTable3 VARCHAR(100)," +
					" sourceId4 UUID," +
					" sourceTable4 VARCHAR(100)," +
					" PRIMARY KEY (id, entity, class))");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT entity, class FROM " + getTableName() + " AS bottom WHERE bottom.entity = t1.entity AND bottom.class = t1.class)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, entity, class) " +
					" SELECT MIN(id), entity, class " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY entity, class");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=4; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT b.theid, '" + RelationName.classAssertionEnt + "' AS table,");
						sql.append(" sourceId" + i + ", sourceTable" + i + "");
						sql.append("\n FROM " + delta.getDeltaName() + " AS t");
						sql.append("\n\t INNER JOIN (");
						sql.append("\n\t\t SELECT MIN(id) AS theid, entity, class ");
						sql.append("\n\t\t FROM " + delta.getDeltaName());
						sql.append("\n\t\t GROUP BY entity, class");
						sql.append("\n\t ) AS b ON b.entity = t.entity AND b.class = t.class");
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
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected String existsImpl(String... args) {
		if (args.length == 1) {
			return "SELECT entity FROM classAssertionEnt WHERE entity = '" + args[0] + "'";
		} else if (args.length == 2) {
			return "SELECT entity, class FROM classAssertionEnt WHERE entity = '" + args[0] + "' AND class = '" + args[1] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
