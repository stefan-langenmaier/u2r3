package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class ListRelation extends Relation {
	static Logger logger = Logger.getLogger(ListRelation.class);
	
	protected ListRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "list";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, name VARCHAR(100), element VARCHAR(100), PRIMARY KEY (name, element))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (name, element) VALUES (?, ?)");

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
					" name VARCHAR(100)," +
					" element VARCHAR(100)," +
					" nameSourceId UUID," +
					" nameSourceTable VARCHAR(100)," +
					" elementSourceId UUID," +
					" elementSourceTable VARCHAR(100)," +
					" PRIMARY KEY (name, element))");
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
