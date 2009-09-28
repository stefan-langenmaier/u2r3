package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class IntersectionOfRelation extends Relation {
	static Logger logger = Logger.getLogger(IntersectionOfRelation.class);
	
	protected IntersectionOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "intersectionOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, class VARCHAR(100), list VARCHAR(100), PRIMARY KEY (class, list))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, list) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "" +
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" class VARCHAR(100)," +
					" list VARCHAR(100)," +
					" classSourceId UUID," +
					" classSourceTable VARCHAR(100)," +
					" listSourceId UUID," +
					" listSourceTable VARCHAR(100)," +
					" PRIMARY KEY (class, list))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		throw new U2R3NotImplementedException();

	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
