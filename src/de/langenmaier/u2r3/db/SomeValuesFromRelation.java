package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class SomeValuesFromRelation extends Relation {
	
	protected SomeValuesFromRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "someValuesFrom";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" part TEXT," +
					" property TEXT," +
					" total TEXT," +
					" PRIMARY KEY (part, total))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (part, property, total) VALUES (?, ?, ?)");

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
			if (ce instanceof  OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (svf.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(svf.getProperty()).toString());
					handleAnonymousObjectPropertyExpression(svf.getProperty());
				} else {
					addStatement.setString(2, svf.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				if (svf.getFiller().isAnonymous()) {
					addStatement.setString(3, nidMapper.get(svf.getFiller()).toString());
					handleAnonymousClassExpression(svf.getFiller());
				} else {
					addStatement.setString(3, svf.getFiller().asOWLClass().getIRI().toString());
				}
				
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
