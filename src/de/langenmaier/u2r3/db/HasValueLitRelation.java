package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

public class HasValueLitRelation extends Relation {
	
	protected HasValueLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "hasValueLit";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" class TEXT," +
					" property TEXT, " +
					" value TEXT," +
					" language TEXT," +
					" type TEXT," +
					" PRIMARY KEY (id, class, property, value));" +
					" CREATE HASH INDEX " + getTableName() + "_class ON " + getTableName() + "(class);" +
					" CREATE HASH INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE HASH INDEX " + getTableName() + "_value ON " + getTableName() + "(value);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, property, value, language, type) VALUES (?, ?, ?, ?, ?)");

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
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
