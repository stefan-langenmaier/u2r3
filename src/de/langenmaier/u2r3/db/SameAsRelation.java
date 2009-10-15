package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SameAsRelation extends Relation {
	
	protected SameAsRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "sameAs";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, left VARCHAR(100), right VARCHAR(100), PRIMARY KEY (left, right))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		OWLSameIndividualAxiom naxiom = (OWLSameIndividualAxiom) axiom;
		for (OWLIndividual ind : naxiom.getIndividuals()) {
			addStatement.setString(1, ind.asNamedIndividual().getIRI().toString());
			addStatement.setString(2, ind.asNamedIndividual().getIRI().toString());
			logger.trace(addStatement.toString());
			addStatement.executeUpdate();
			reasonProcessor.add(new AdditionReason(this));
		}
		return false;
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left VARCHAR(100)," +
					" right VARCHAR(100)," +
					" leftSourceId UUID," +
					" leftSourceTable VARCHAR(100)," +
					" rightSourceId UUID," +
					" rightSourceTable VARCHAR(100)," +
					" PRIMARY KEY (left, right))");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT left, right FROM " + getTableName() + " AS bottom WHERE bottom.left = t1.left AND bottom.right = t1.right)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, left, right) SELECT id, left, right FROM ( " +
					" SELECT id, left, right " +
					" FROM " + delta.getDeltaName() + " " +
					")");			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					//leftSource
					sql = "SELECT id, '" + RelationName.sameAs + "' AS table, leftSourceId, leftSourceTable FROM " + delta.getDeltaName();
					relationManager.addHistory(sql);
					
					//rightSource
					sql = "SELECT id, '" + RelationName.sameAs + "' AS table, rightSourceId, rightSourceTable FROM " + delta.getDeltaName();
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
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 1) {
			return "SELECT left FROM " + getTableName() + " WHERE left = '" + args[0] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
