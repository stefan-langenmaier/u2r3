package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

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

	public boolean addImpl(OWLAxiom axiom) throws SQLException {
			OWLSubClassOfAxiom naxiom = (OWLSubClassOfAxiom) axiom;
			addStatement.setString(1, naxiom.getSubClass().asOWLClass().getURI().toString());
			addStatement.setString(2, naxiom.getSuperClass().asOWLClass().getURI().toString());
			return true;
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
		throw new U2R3NotImplementedException();
		
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
