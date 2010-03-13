package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SubPropertyRelation extends Relation {
	static Logger logger = Logger.getLogger(SubPropertyRelation.class);
	
	protected SubPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "subProperty";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" sub TEXT," +
					" super TEXT," +
					" PRIMARY KEY (sub, super));" +
					" CREATE INDEX " + getTableName() + "_sub ON " + getTableName() + "(sub);" +
					" CREATE INDEX " + getTableName() + "_super ON " + getTableName() + "(super);");

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" sub TEXT," +
		" super TEXT," +
		" sourceId1 BIGINT," +
		" sourceTable1 VARCHAR(100)," +
		" sourceId2 BIGINT," +
		" sourceTable2 VARCHAR(100)," +
		" PRIMARY KEY (sub, super));" +
		" CREATE INDEX " + table + "_sub ON " + table + "(sub);" +
		" CREATE INDEX " + table + "_super ON " + table + "(super);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (sub, super) VALUES (?, ?)";
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
			OWLSubObjectPropertyOfAxiom naxiom = (OWLSubObjectPropertyOfAxiom) axiom;
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
				if (naxiom.getSubProperty().isAnonymous()) {
					add.setString(1, nidMapper.get(naxiom.getSubProperty()).toString());
					handleAddAnonymousObjectPropertyExpression(naxiom.getSubProperty());
				} else {
					add.setString(1, naxiom.getSubProperty().asOWLObjectProperty().getIRI().toString());
				}
				if (naxiom.getSuperProperty().isAnonymous()) {
					add.setString(2, nidMapper.get(naxiom.getSuperProperty()).toString());
					handleAddAnonymousObjectPropertyExpression(naxiom.getSuperProperty());
				} else {
					add.setString(2, naxiom.getSuperProperty().asOWLObjectProperty().getIRI().toString());
				}
			}
			return AdditionMode.ADD;
		} else if (axiom instanceof OWLSubDataPropertyOfAxiom) {
			OWLSubDataPropertyOfAxiom naxiom = (OWLSubDataPropertyOfAxiom) axiom;
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
				if (naxiom.getSubProperty().isAnonymous()) {
					add.setString(1, nidMapper.get(naxiom.getSubProperty()).toString());
					handleAddAnonymousDataPropertyExpression(naxiom.getSubProperty());
				} else {
					add.setString(1, naxiom.getSubProperty().asOWLDataProperty().getIRI().toString());
				}
				if (naxiom.getSuperProperty().isAnonymous()) {
					add.setString(2, nidMapper.get(naxiom.getSuperProperty()).toString());
					handleAddAnonymousDataPropertyExpression(naxiom.getSuperProperty());
				} else {
					add.setString(2, naxiom.getSuperProperty().asOWLDataProperty().getIRI().toString());
				}
			}
			return AdditionMode.ADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT sub, super FROM " + getTableName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t1.super)");
				
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, sub, super) " +
					" SELECT MIN(id), sub, super " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY sub, super");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=2; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT id, '" + RelationName.subProperty + "' AS colTable,");
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
		if (ax instanceof OWLSubObjectPropertyOfAxiom) {
			OWLSubObjectPropertyOfAxiom nax = (OWLSubObjectPropertyOfAxiom) ax;
			String subProperty = null;
			String superProperty = null;
			String tableId = TableId.getId();
			
			if (!nax.getSubProperty().isAnonymous()) {
				subProperty = nax.getSubProperty().asOWLObjectProperty().getIRI().toString();
			}
			
			if (!nax.getSuperProperty().isAnonymous()) {
				superProperty = nax.getSuperProperty().asOWLObjectProperty().getIRI().toString();
			}			
			
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			if (subProperty != null) {
				sql.append("sub='" + subProperty + "' ");
			} else {
				sql.append(" EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getSubProperty(), tableId, "sub");
			}
			
			if (superProperty != null) {
				sql.append(" AND super='" + superProperty + "'");
			} else {
				sql.append(" AND EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getSuperProperty(), tableId, "super");
			}
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		if (ax instanceof OWLSubDataPropertyOfAxiom) {
			OWLSubDataPropertyOfAxiom nax = (OWLSubDataPropertyOfAxiom) ax;
			String subProperty = null;
			String superProperty = null;
			String tableId = TableId.getId();
			
			if (!nax.getSubProperty().isAnonymous()) {
				subProperty = nax.getSubProperty().asOWLDataProperty().getIRI().toString();
			}
			
			if (!nax.getSuperProperty().isAnonymous()) {
				superProperty = nax.getSuperProperty().asOWLDataProperty().getIRI().toString();
			}			
			
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			if (subProperty != null) {
				sql.append("sub='" + subProperty + "' ");
			} else {
				sql.append(" EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getSubProperty(), tableId, "sub");
			}
			
			if (superProperty != null) {
				sql.append(" AND super='" + superProperty + "'");
			} else {
				sql.append(" AND EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getSuperProperty(), tableId, "super");
			}
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}

}
