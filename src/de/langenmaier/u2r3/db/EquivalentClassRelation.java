package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EquivalentClassRelation extends Relation {
	static Logger logger = Logger.getLogger(EquivalentClassRelation.class);
	
	protected EquivalentClassRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "equivalentClass";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" PRIMARY KEY (colLeft, colRight));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(colLeft);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(colRight);");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight) VALUES (?, ?)");

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
		" PRIMARY KEY (colLeft, colRight));" +
		" CREATE INDEX " + table + "_left ON " + table + "(colLeft);" +
		" CREATE INDEX " + table + "_right ON " + table + "(colRight);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colLeft, colRight) VALUES (?, ?)";
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLEquivalentClassesAxiom) {
			OWLEquivalentClassesAxiom naxiom = (OWLEquivalentClassesAxiom) axiom;
			for (OWLClassExpression ce1 : naxiom.getClassExpressions()) {
				for (OWLClassExpression ce2 : naxiom.getClassExpressions()) {
					if (!ce1.equals(ce2)) {
						PreparedStatement add = addStatement;

						for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
							if (ce1.isAnonymous()) {
								add.setString(1, nidMapper.get(ce1).toString());
							} else {
								add.setString(1, ce1.asOWLClass().getIRI().toString());
							}
							if (ce2.isAnonymous()) {
								add.setString(2, nidMapper.get(ce2).toString());
							} else {
								add.setString(2, ce2.asOWLClass().getIRI().toString());
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
				if (ce1.isAnonymous()) {
					handleAddAnonymousClassExpression(ce1);
				}
			}
			return AdditionMode.NOADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT colLeft, colRight FROM " + getTableName() + " AS bottom WHERE bottom.colLeft = t1.colLeft AND bottom.colRight = t1.colRight)");

			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, colLeft, colRight) " +
					" SELECT MIN(id), colLeft, colRight " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY colLeft, colRight");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=2; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT id, '" + RelationName.equivalentClass + "' AS colTable,");
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
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLEquivalentClassesAxiom) {
			OWLEquivalentClassesAxiom nax = (OWLEquivalentClassesAxiom) ax;
			String left = null;
			String right = null;
			boolean first = true;
			String tableId = TableId.getId();	
			
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			
			for(OWLClassExpression leftClass : nax.getClassExpressions()) {
				for(OWLClassExpression rightClass : nax.getClassExpressions()) {
					if (!leftClass.equals(rightClass)) {
						if (!leftClass.isAnonymous()) {
							left = leftClass.asOWLClass().getIRI().toString();
						} else {
							left = null;
						}
						
						if (!rightClass.isAnonymous()) {
							right = rightClass.asOWLClass().getIRI().toString();
						} else {
							right = null;
						}
						
						if (first) {
							first = false;
						} else {
							sql.append(" AND ");
						}
						
						sql.append("(");
						
						if (left != null) {
							sql.append("colLeft='" + left + "'");
						} else {
							sql.append("EXISTS ");
							handleSubAxiomLocationImpl(sql, leftClass, tableId, "colLeft");
						}
						sql.append(" AND ");
						if (right != null) {
							sql.append("colRight='" + right + "'");
						} else {
							sql.append("EXISTS ");
							handleSubAxiomLocationImpl(sql, rightClass, tableId, "colRight");
						}
						
						sql.append(")");

					}
				}
			}

			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}


}
