package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PropertyDomainRelation extends Relation {
	static Logger logger = Logger.getLogger(PropertyDomainRelation.class);
	
	protected PropertyDomainRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyDomain";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" property TEXT," +
					" domain TEXT," +
					" PRIMARY KEY (property, domain));" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_domain ON " + getTableName() + "(domain);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (property, domain) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDataPropertyDomainAxiom) {
			OWLDataPropertyDomainAxiom naxiom = (OWLDataPropertyDomainAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			addStatement.setString(2, naxiom.getDomain().asOWLClass().getIRI().toString());
		} else if (axiom instanceof OWLObjectPropertyDomainAxiom) {
			OWLObjectPropertyDomainAxiom naxiom = (OWLObjectPropertyDomainAxiom) axiom;
			addStatement.setString(1, naxiom.getProperty().asOWLObjectProperty().getIRI().toString());
			addStatement.setString(2, naxiom.getDomain().asOWLClass().getIRI().toString());
		}
		return AdditionMode.ADD;
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" property TEXT," +
					" domain TEXT," +
					" sourceId1 UUID, " +
					" sourceTable1 VARCHAR(100), " +
					" sourceId2 UUID, " +
					" sourceTable2 VARCHAR(100), " +
					" PRIMARY KEY (property, domain));" +
					" CREATE HASH INDEX " + getDeltaName(id) + "_property ON " + getDeltaName(id) + "(property);" +
					" CREATE HASH INDEX " + getDeltaName(id) + "_domain ON " + getDeltaName(id) + "(domain);");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT property, domain FROM " + getTableName() + " AS bottom WHERE bottom.property = t1.property AND bottom.domain = t1.domain)");
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, property, domain ) " +
					" SELECT MIN(id), property, domain  " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY property, domain ");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					StringBuilder sql;
					
					for (int i=1; i<=2; ++i) {
						//source
						sql = new StringBuilder();
						sql.append("SELECT id, '" + RelationName.propertyDomain + "' AS table,");
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
			return "SELECT property, domain FROM " + getTableName() + " WHERE property = '" + args[0] + "' AND domain = '" + args[1] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
