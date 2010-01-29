package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PropertyRangeRelation extends Relation {
	static Logger logger = Logger.getLogger(PropertyRangeRelation.class);
	
	protected PropertyRangeRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyRange";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" property TEXT," +
					" range TEXT," +
					" PRIMARY KEY (property, range));" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_range ON " + getTableName() + "(range);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (property, range) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDataPropertyRangeAxiom) {
			OWLDataPropertyRangeAxiom naxiom = (OWLDataPropertyRangeAxiom) axiom;
			
			if (naxiom.getProperty().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getProperty()).toString());
			} else {
				addStatement.setString(1, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getRange().asOWLDatatype().getIRI().toString());
		} else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
			OWLObjectPropertyRangeAxiom naxiom = (OWLObjectPropertyRangeAxiom) axiom;
			if (naxiom.getProperty().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getProperty()).toString());
			} else {
				addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getIRI().toString());
			}
			
			if (naxiom.getRange().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getRange()).toString());
			} else {
				addStatement.setString(2, naxiom.getRange().asOWLClass().getIRI().toString());
			}
			
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			if (naxiom.getProperty().isAnonymous()) {
				handleAnonymousObjectPropertyExpression(naxiom.getProperty());
			}
			if (naxiom.getRange().isAnonymous()) {
				handleAnonymousClassExpression(naxiom.getRange());
			}
			
		} else {
			throw new U2R3NotImplementedException();
		}
		return AdditionMode.NOADD;
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" property TEXT," +
					" range TEXT," +
					" sourceId1 BIGINT, " +
					" sourceTable1 VARCHAR(100), " +
					" sourceId2 BIGINT, " +
					" sourceTable2 VARCHAR(100), " +
					" PRIMARY KEY (property, range));" +
					" CREATE INDEX " + getDeltaName(id) + "_property ON " + getDeltaName(id) + "(property);" +
					" CREATE INDEX " + getDeltaName(id) + "_range ON " + getDeltaName(id) + "(range);");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT property, range FROM " + getTableName() + " AS bottom WHERE bottom.property = t1.property AND bottom.range = t1.range)");
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, property, range ) " +
					" SELECT MIN(id), property, range  " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY property, range ");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=2; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT id, '" + RelationName.propertyRange + "' AS colTable,");
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
			return "SELECT property, range FROM " + getTableName() + " WHERE property = '" + args[0] + "' AND range = '" + args[1] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
