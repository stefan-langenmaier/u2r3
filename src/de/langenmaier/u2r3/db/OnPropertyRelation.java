package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class OnPropertyRelation extends Relation {
	
	protected OnPropertyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "onProperty";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" class TEXT," +
					" property TEXT," +
					" PRIMARY KEY (class, property))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, property) VALUES (?, ?)");

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
			if (ce instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (svf.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(svf.getProperty()).toString());
					handleAnonymousObjectPropertyExpression(svf.getProperty());
				} else {
					addStatement.setString(2, svf.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
			} else if (ce instanceof OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (avf.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(avf.getProperty()).toString());
					handleAnonymousObjectPropertyExpression(avf.getProperty());
				} else {
					addStatement.setString(2, avf.getProperty().asOWLObjectProperty().getIRI().toString());
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
