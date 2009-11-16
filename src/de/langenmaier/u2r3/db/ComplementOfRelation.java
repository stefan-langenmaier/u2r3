package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class ComplementOfRelation extends Relation {
	
	protected ComplementOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "complementOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					"id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left VARCHAR(100)," +
					" right VARCHAR(100)," +
					" PRIMARY KEY (left, right))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

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
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left VARCHAR(100)," +
					" right VARCHAR(100)," +
					" leftSourceId UUID," +
					" leftSourceTable VARCHAR(100)," +
					" rightSourceId UUID," +
					" rightSourceTable VARCHAR(100)," +
					" PRIMARY KEY (left, right))");
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
	
	@Override
	public void add(OWLObject ce) {
		OWLObjectComplementOf oco = (OWLObjectComplementOf) ce;
		try {
			addStatement.setString(1, nidMapper.get(ce).toString());
			if (oco.getOperand().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(oco.getOperand()).toString());
			} else {
				addStatement.setString(2, oco.getOperand().asOWLClass().getIRI().toString());
			}
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			if (oco.getOperand().isAnonymous()) {
				handleAnonymousClassExpression(oco.getOperand());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
