package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectHasValue;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class HasValueEntRelation extends Relation {
	
	protected HasValueEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "hasValueEnt";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colClass TEXT," +
					" property TEXT, " +
					" value TEXT," +
					" PRIMARY KEY (id, colClass, property, value));" +
					" CREATE INDEX " + getTableName() + "_class ON " + getTableName() + "(colClass);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_value ON " + getTableName() + "(value);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colClass, property, value) VALUES (?, ?, ?)");

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

	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLObjectHasValue) {
				OWLObjectHasValue hv = (OWLObjectHasValue) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (hv.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(hv.getProperty()).toString());
				} else {
					addStatement.setString(2, hv.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				if (hv.getValue().isAnonymous()) {
					addStatement.setString(3, hv.getValue().asOWLAnonymousIndividual().getID().toString());
				} else {
					addStatement.setString(3, hv.getValue().asOWLNamedIndividual().getIRI().toString());
				}
				
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (hv.getProperty().isAnonymous()) {
					handleAnonymousObjectPropertyExpression(hv.getProperty());
				}
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
