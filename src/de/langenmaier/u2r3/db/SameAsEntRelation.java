package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
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

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
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
		" CREATE INDEX " + table + "_left ON " + table + "(colLeft);" +
		" CREATE INDEX " + table + "_right ON " + table + "(colRight)";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colLeft, colRight) VALUES (?, ?)";
	}

	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLSameIndividualAxiom) {
			OWLSameIndividualAxiom naxiom = (OWLSameIndividualAxiom) axiom;
			for (OWLIndividual ind1 : naxiom.getIndividuals()) {
				for (OWLIndividual ind2 : naxiom.getIndividuals()) {
					if (!ind1.equals(ind2)) {
						PreparedStatement add = addStatement;

						for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
							if (ind1.isAnonymous()) {
								add.setString(1, ind1.asOWLAnonymousIndividual().getID().toString());
							} else {
								add.setString(1, ind1.asOWLNamedIndividual().getIRI().toString());
							}
							if (ind2.isAnonymous()) {
								add.setString(2, ind2.asOWLAnonymousIndividual().getID().toString());
							} else {
								add.setString(2, ind2.asOWLNamedIndividual().getIRI().toString());
							}
							
							add.execute();
						}
						if (reasoner.isAdditionMode()) {
							reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
						} else {
							reasonProcessor.add(new AdditionReason(this));
						}
					}
				}
			}

			return AdditionMode.NOADD;
		} else {
			throw new U2R3NotImplementedException();
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
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLSameIndividualAxiom) {
			OWLSameIndividualAxiom nax = (OWLSameIndividualAxiom) ax;
			String left = null;
			String right = null;
			String tableId = TableId.getId();
			
			if (nax.getIndividuals().size() <= 2) {
				if (!nax.getIndividualsAsList().get(0).isAnonymous()) {
					left = nax.getIndividualsAsList().get(0).asOWLNamedIndividual().getIRI().toString();
				}
				
				if (nax.getIndividuals().size() == 1) {
					right = left;
				} else {
					if (!nax.getIndividualsAsList().get(1).isAnonymous()) {
						right = nax.getIndividualsAsList().get(1).asOWLNamedIndividual().getIRI().toString();
					}		
				}
				
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
				sql.append("\nFROM  " + getTableName() + " AS " + tableId);
				sql.append("\nWHERE ");
				if (left != null) {
					sql.append("colLeft='" + left + "' ");
				} else {
					sql.append(" EXISTS ");
					handleSubAxiomLocationImpl(sql, nax.getIndividualsAsList().get(0), tableId, "colLeft");
				}
				
				if (right != null) {
					sql.append(" AND colRight='" + right + "'");
				} else {
					sql.append(" AND EXISTS ");
					handleSubAxiomLocationImpl(sql, nax.getIndividualsAsList().get(1), tableId, "colRight");
				}
				PreparedStatement stmt = conn.prepareStatement(sql.toString());
				return stmt;
			} else {
				return relationManager.getRelation(RelationName.members).getAxiomLocation(ax);
			}
		}
		throw new U2R3RuntimeException();
	}

}
