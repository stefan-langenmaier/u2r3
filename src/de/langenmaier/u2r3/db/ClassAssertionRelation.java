package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClassAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(ClassAssertionRelation.class);
	
	protected ClassAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "classAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, type) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
			OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getIndividual().asNamedIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getClassExpression().asOWLClass().getIRI().toURI().toString());
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + 
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" class VARCHAR(100)," +
					" type VARCHAR(100)," +
					" classSourceId UUID," +
					" classSourceTable VARCHAR(100)," +
					" typeSourceId UUID," +
					" typeSourceTable VARCHAR(100)," +
					" PRIMARY KEY (class, type))");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT class, type FROM " + getTableName() + " AS bottom WHERE bottom.class = t1.class AND bottom.type = t1.type)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, class, type) " +
					" SELECT id, class, type " +
					" FROM " + delta.getDeltaName());

			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					//remove rows without history
					sql = "DELETE FROM " + delta.getDeltaName() + " WHERE classSourceId IS NULL";
					rows = stmt.executeUpdate(sql);				
					
					//subjectSource
					sql = "SELECT id, '" + RelationName.classAssertion + "' AS table, classSourceId, classSourceTable FROM " + delta.getDeltaName();
					relationManager.addHistory(sql);
					
					//superSource
					sql = "SELECT id, '" + RelationName.classAssertion + "' AS table, typeSourceId, typeSourceTable FROM " + delta.getDeltaName();
					relationManager.addHistory(sql);
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

}
