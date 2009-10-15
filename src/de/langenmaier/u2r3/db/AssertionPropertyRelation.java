package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class AssertionPropertyRelation extends Relation {
	
	protected AssertionPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "assertionProperty";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" name VARCHAR(100)," +
					" property VARCHAR(100)," +
					" PRIMARY KEY (name, property))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (name, property) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();

	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" name VARCHAR(100)," +
					" property VARCHAR(100)," +
					" nameSourceId UUID," +
					" nameSourceTable VARCHAR(100)," +
					" propertySourceId UUID," +
					" propertySourceTable VARCHAR(100)," +
					" PRIMARY KEY (name, property))");
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
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
