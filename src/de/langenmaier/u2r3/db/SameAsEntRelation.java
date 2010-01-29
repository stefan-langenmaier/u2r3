package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SameAsEntRelation extends Relation {
	
	protected SameAsEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "sameAsEnt";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" PRIMARY KEY (colLeft, colRight));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(colLeft);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(colRight)");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLSameIndividualAxiom) {
			OWLSameIndividualAxiom naxiom = (OWLSameIndividualAxiom) axiom;
			for (OWLIndividual ind1 : naxiom.getIndividuals()) {
				for (OWLIndividual ind2 : naxiom.getIndividuals()) {
					if (!ind1.equals(ind2)) {
						if (ind1.isAnonymous()) {
							addStatement.setString(1, ind1.asOWLAnonymousIndividual().getID().toString());
						} else {
							addStatement.setString(1, ind1.asOWLNamedIndividual().getIRI().toString());
						}
						if (ind2.isAnonymous()) {
							addStatement.setString(2, ind2.asOWLAnonymousIndividual().getID().toString());
						} else {
							addStatement.setString(2, ind2.asOWLNamedIndividual().getIRI().toString());
						}
						
						addStatement.execute();
						reasonProcessor.add(new AdditionReason(this));
					}
				}
			}

			return AdditionMode.NOADD;
		} else {
			throw new U2R3NotImplementedException();
		}

	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			// bis zu 6 Quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" sourceId1 BIGINT," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 BIGINT," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 BIGINT," +
					" sourceTable3 VARCHAR(100)," +
					" sourceId4 BIGINT," +
					" sourceTable4 VARCHAR(100)," +
					" sourceId5 BIGINT," +
					" sourceTable5 VARCHAR(100)," +
					" sourceId6 BIGINT," +
					" sourceTable6 VARCHAR(100)," +
					" PRIMARY KEY (id, colLeft, colRight));" +
					" CREATE INDEX " + getDeltaName(id) + "_left ON " + getDeltaName(id) + "(colLeft);" +
					" CREATE INDEX " + getDeltaName(id) + "_right ON " + getDeltaName(id) + "(colRight)");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT colLeft, colRight FROM " + getTableName() + " AS bottom WHERE bottom.colLeft = t1.colLeft AND bottom.colRight = t1.colRight)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, colLeft, colRight) " +
					" SELECT MIN(id), colLeft, colRight " +
					" FROM " + delta.getDeltaName() + " " +
					" GROUP BY colLeft, colRight");			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=6; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT b.theid, '" + RelationName.sameAsEnt + "' AS colTable,");
						sql.append(" sourceId" + i + ", sourceTable" + i + "");
						sql.append("\n FROM " + delta.getDeltaName() + " AS t");
						sql.append("\n\t INNER JOIN (");
						sql.append("\n\t\t SELECT MIN(id) AS theid, colLeft, colRight");
						sql.append("\n\t\t FROM " + delta.getDeltaName());
						sql.append("\n\t\t GROUP BY colLeft, colRight");
						sql.append("\n\t ) AS b ON b.colLeft = t.colLeft AND b.colRight = t.colRight");
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
		if (args.length == 1) {
			return "SELECT colLeft FROM " + getTableName() + " WHERE colLeft = '" + args[0] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
