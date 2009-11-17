package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class SubPropertyRelation extends Relation {
	static Logger logger = Logger.getLogger(SubPropertyRelation.class);
	
	protected SubPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "subProperty";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" sub TEXT," +
					" super TEXT," +
					" PRIMARY KEY (sub, super))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (sub, super) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
			OWLSubObjectPropertyOfAxiom naxiom = (OWLSubObjectPropertyOfAxiom) axiom;
			if (naxiom.getSubProperty().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getSubProperty()).toString());
				handleAnonymousObjectPropertyExpression(naxiom.getSubProperty());
			} else {
				addStatement.setString(1, naxiom.getSubProperty().asOWLObjectProperty().getIRI().toString());
			}
			if (naxiom.getSuperProperty().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getSuperProperty()).toString());
				handleAnonymousObjectPropertyExpression(naxiom.getSuperProperty());
			} else {
				addStatement.setString(2, naxiom.getSuperProperty().asOWLObjectProperty().getIRI().toString());
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
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" sub TEXT," +
					" super TEXT," +
					" sourceId1 UUID," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 UUID," +
					" sourceTable2 VARCHAR(100)," +
					" PRIMARY KEY (sub, super))");
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
						sql.append("SELECT id, '" + RelationName.subProperty + "' AS table,");
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
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 2) {
			return "SELECT sub, super FROM " + getTableName() + " WHERE sub = '" + args[0] + "' AND super = '" + args[1] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
