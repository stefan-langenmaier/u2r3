package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class MaxCardinalityRelation extends Relation {
	
	protected MaxCardinalityRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "maxCardinality";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					"id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" class TEXT," +
					" property TEXT, " +
					" value TEXT," +
					" PRIMARY KEY (class, property, value))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, property, value) VALUES (?, ?, ?)");

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
	
	@Override
	public void add(OWLObject ce) {
		try {
			addStatement.setString(1, nidMapper.get(ce).toString());
			
			
			if (ce instanceof OWLObjectMaxCardinality) {
				OWLObjectMaxCardinality mc = (OWLObjectMaxCardinality) ce;
				
				if (mc.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(mc.getProperty()).toString());
					handleAnonymousObjectPropertyExpression(mc.getProperty());
				} else {
					addStatement.setString(2, mc.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				addStatement.setString(3, Integer.toString(mc.getCardinality()));
			} else {
				throw new U2R3NotImplementedException();
			}
			
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
