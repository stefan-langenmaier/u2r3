package de.langenmaier.u2r3.db;

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
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EquivalentPropertyRelation extends Relation {
	static Logger logger = Logger.getLogger(EquivalentPropertyRelation.class);
	
	protected EquivalentPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "equivalentProperty";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" left TEXT," +
					" right TEXT," +
					" PRIMARY KEY (left, right));" +
					" CREATE HASH INDEX " + getTableName() + "_left ON " + getTableName() + "(left);" +
					" CREATE HASH INDEX " + getTableName() + "_right ON " + getTableName() + "(right);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

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
					handleAnonymousObjectPropertyExpression(pe1);
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
					handleAnonymousDataPropertyExpression(pe1);
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
					" left TEXT," +
					" right TEXT," +
					" sourceId1 UUID," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 UUID," +
					" sourceTable2 VARCHAR(100)," +
					" PRIMARY KEY (left, right));" +
					" CREATE HASH INDEX " + getDeltaName(id) + "_left ON " + getDeltaName(id) + "(left);" +
					" CREATE HASH INDEX " + getDeltaName(id) + "_right ON " + getDeltaName(id) + "(right);");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void merge(DeltaRelation delta) {
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT left, right FROM " + getTableName() + " AS bottom WHERE bottom.left = t1.left AND bottom.right = t1.right)");

			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, left, right) " +
					" SELECT MIN(id), left, right " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY left, right");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=2; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT id, '" + RelationName.equivalentProperty + "' AS table,");
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
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 2) {
			return "SELECT left, right FROM " + getTableName() + " WHERE left = '" + args[0] + "' AND right = '" + args[1] + "'";
		}
		throw new U2R3NotImplementedException();
	}


}
