package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class ObjectPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(ObjectPropertyAssertionRelation.class);
	
	protected ObjectPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "objectPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom naxiom = (OWLObjectPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getURI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLObjectProperty().getURI().toString());
			if (naxiom.getObject().isAnonymous()) {
				addStatement.setString(3, naxiom.getObject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(3, naxiom.getObject().asNamedIndividual().getURI().toString());
			}
		}
		return true;
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			//max 3 Quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "" +
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" sourceId1 UUID," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 UUID," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 UUID," +
					" sourceTable3 VARCHAR(100)," +
					" PRIMARY KEY (subject, property, object))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 3) {
			return "SELECT '1' FROM " + getTableName() + " WHERE subject = '" + args[0] + "' AND property = '" + args[1] + "' AND object ='" + args[2] + "'";
		}
		throw new U2R3NotImplementedException();
	}

}
