package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EquivalentPropertyRelation extends Relation {
	static Logger logger = Logger.getLogger(EquivalentPropertyRelation.class);
	
	protected EquivalentPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "equivalentProperty";
			
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

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLEquivalentObjectPropertiesAxiom) {
			OWLEquivalentObjectPropertiesAxiom naxiom = (OWLEquivalentObjectPropertiesAxiom) axiom;
			for (OWLObjectPropertyExpression pe1 : naxiom.getProperties()) {
				for (OWLObjectPropertyExpression pe2 : naxiom.getProperties()) {
					if (!pe1.equals(pe2)) {
						if (pe1.isAnonymous()) {
							addStatement.setString(1, nidMapper.get(pe1).toString());
						} else {
							addStatement.setString(1, pe1.asOWLObjectProperty().getIRI().toString());
						}
						if (pe2.isAnonymous()) {
							addStatement.setString(2, nidMapper.get(pe2).toString());
						} else {
							addStatement.setString(2, pe2.asOWLObjectProperty().getIRI().toString());
						}
						addStatement.execute();
						reasonProcessor.add(new AdditionReason(this));
					}
				}
				if (pe1.isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(pe1);
				}
			}
			return AdditionMode.NOADD;
		} else if (axiom instanceof OWLEquivalentDataPropertiesAxiom) {
			OWLEquivalentDataPropertiesAxiom naxiom = (OWLEquivalentDataPropertiesAxiom) axiom;
			for (OWLDataPropertyExpression pe1 : naxiom.getProperties()) {
				for (OWLDataPropertyExpression pe2 : naxiom.getProperties()) {
					if (!pe1.equals(pe2)) {
						if (pe1.isAnonymous()) {
							addStatement.setString(1, nidMapper.get(pe1).toString());
						} else {
							addStatement.setString(1, pe1.asOWLDataProperty().getIRI().toString());
						}
						if (pe2.isAnonymous()) {
							addStatement.setString(2, nidMapper.get(pe2).toString());
						} else {
							addStatement.setString(2, pe2.asOWLDataProperty().getIRI().toString());
						}
						addStatement.execute();
						reasonProcessor.add(new AdditionReason(this));
					}
				}
				if (pe1.isAnonymous()) {
					handleAddAnonymousDataPropertyExpression(pe1);
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
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" sourceId1 BIGINT," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 BIGINT," +
					" sourceTable2 VARCHAR(100)," +
					" PRIMARY KEY (colLeft, colRight));" +
					" CREATE INDEX " + getDeltaName(id) + "_left ON " + getDeltaName(id) + "(colLeft);" +
					" CREATE INDEX " + getDeltaName(id) + "_right ON " + getDeltaName(id) + "(colRight);");
		} catch (SQLException e) {
			e.printStackTrace();
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
						sql.append("SELECT id, '" + RelationName.equivalentProperty + "' AS colTable,");
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
		if (ax instanceof OWLEquivalentObjectPropertiesAxiom) {
			OWLEquivalentObjectPropertiesAxiom nax = (OWLEquivalentObjectPropertiesAxiom) ax;
			String left = null;
			String right = null;
			boolean first = true;
			String tableId = TableId.getId();	
			
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			
			for(OWLObjectPropertyExpression leftProperty : nax.getProperties()) {
				for(OWLObjectPropertyExpression rightProperty : nax.getPropertiesMinus(leftProperty)) {
					if (!leftProperty.isAnonymous()) {
						left = leftProperty.asOWLObjectProperty().getIRI().toString();
					} else {
						left = null;
					}
					
					if (!rightProperty.isAnonymous()) {
						right = rightProperty.asOWLObjectProperty().getIRI().toString();
					} else {
						right = null;
					}
					
					if (first) {
						first = false;
					} else {
						sql.append(" AND "); //XXX
					}
					
					sql.append("(");
					
					if (left != null) {
						sql.append("colLeft='" + left + "'");
					} else {
						sql.append("EXISTS ");
						handleSubAxiomLocationImpl(sql, leftProperty, tableId, "colLeft");
					}
					sql.append(" AND ");
					if (right != null) {
						sql.append("colRight='" + right + "'");
					} else {
						sql.append("EXISTS ");
						handleSubAxiomLocationImpl(sql, rightProperty, tableId, "colRight");
					}
					
					sql.append(")");
				}
			}

			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}


}
