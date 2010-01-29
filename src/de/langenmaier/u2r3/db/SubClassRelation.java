package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SubClassRelation extends Relation {
	static Logger logger = Logger.getLogger(SubClassRelation.class);
	
	protected SubClassRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "subClass";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" sub TEXT," +
					" super TEXT," +
					" PRIMARY KEY (sub, super));" +
					" CREATE INDEX " + getTableName() + "_sub ON " + getTableName() + "(sub);" +
					" CREATE INDEX " + getTableName() + "_super ON " + getTableName() + "(super);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() );

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (sub, super) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom naxiom = (OWLSubClassOfAxiom) axiom;
			if (naxiom.getSubClass().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getSubClass()).toString());
				handleAnonymousClassExpression(naxiom.getSubClass());
			} else {
				addStatement.setString(1, naxiom.getSubClass().asOWLClass().getIRI().toString());
			}
			if (naxiom.getSuperClass().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getSuperClass()).toString());
				handleAnonymousClassExpression(naxiom.getSuperClass());
			} else {
				addStatement.setString(2, naxiom.getSuperClass().asOWLClass().getIRI().toString());
			}

			return AdditionMode.ADD;
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
					" sub TEXT," +
					" super TEXT," +
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
					" PRIMARY KEY (sub, super));" +
					" CREATE INDEX " + getDeltaName(id) + "_sub ON " + getDeltaName(id) + "(sub);" +
					" CREATE INDEX " + getDeltaName(id) + "_super ON " + getDeltaName(id) + "(super);");
		} catch (SQLException e) {
			e.printStackTrace();
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
					" SELECT id, sub, super " +
					" FROM " + delta.getDeltaName());

			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					for (int i=1; i<=5; ++i) {
						//remove rows without history
						//sql = "DELETE FROM " + delta.getDeltaName() + " WHERE sourceId" + i + " IS NULL";
						//rows = stmt.executeUpdate(sql);				
						
						//source
						sql = "SELECT id, '" + RelationName.sameAsEnt + "' AS colTable, sourceId" + i + ", sourceTable" + i + " FROM " + delta.getDeltaName() + " WHERE sourceId" + i + " IS NOT NULL";
						relationManager.addHistory(sql);
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
		if (axiom instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom naxiom = (OWLSubClassOfAxiom) axiom;
			
			String tid = TableId.getId();
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT id");
			sql.append("\nFROM " + getTableName() + " AS " + tid);
			sql.append("\nWHERE EXISTS (");
			getSubSQL(sql, naxiom.getSubClass(), tid, "sub");
			sql.append(") AND super = (");
			getSubSQL(sql, naxiom.getSuperClass(), tid, "super");
			sql.append(")");
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql.toString());
			
			if (rs.next()) {
				relationManager.remove(rs.getLong("id"), RelationName.subClass);
				
				if (naxiom.getSubClass().isAnonymous()) {
					removeAnonymousClassExpression(naxiom.getSubClass());
				}
				
				if (naxiom.getSuperClass().isAnonymous()) {
					removeAnonymousClassExpression(naxiom.getSuperClass());
				}
				
			}
		} else {
			throw new U2R3NotImplementedException();
		}
	}


	@Override
	protected String existsImpl(String... args) {
		if (args.length == 2) {
			return "SELECT sub, super FROM " + getTableName() + " WHERE sub = '" + args[0] + "' AND super = '" + args[1] + "'";
		}
		throw new U2R3NotImplementedException();
	}
	
	@Override
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom nax = (OWLSubClassOfAxiom) ax;
			String subClass = null;
			String superClass = null;
			String tableId = TableId.getId();
			
			if (!nax.getSubClass().isAnonymous()) {
				subClass = nax.getSubClass().asOWLClass().getIRI().toString();
			}
			
			if (!nax.getSuperClass().isAnonymous()) {
				superClass = nax.getSuperClass().asOWLClass().getIRI().toString();
			}			
			
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT uid, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			if (subClass != null) {
				sql.append("sub='" + subClass + "' ");
			} else {
				sql.append(" EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getSubClass(), tableId, "sub");
			}
			
			if (superClass != null) {
				sql.append(" AND super='" + superClass + "'");
			} else {
				sql.append(" AND EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getSuperClass(), tableId, "super");
			}
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			return stmt;
		}
		throw new U2R3RuntimeException();
	}

}
