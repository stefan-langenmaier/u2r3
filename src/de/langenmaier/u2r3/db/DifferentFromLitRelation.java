package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class DifferentFromLitRelation extends Relation {
	
	protected DifferentFromLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "differentFromLit";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left TEXT," +
					" right TEXT," +
					" left_language TEXT," +
					" left_type TEXT," +
					" right_language TEXT," +
					" right_type TEXT," +
					" PRIMARY KEY (id, left, right))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right, left_language, left_type, right_language, right_type) VALUES (?, ?, ?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();

	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
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
