package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

public class HasKeyRelation extends Relation {
	
	protected HasKeyRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "hasKey";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" colClass TEXT," +
					" list TEXT," +
					" PRIMARY KEY (colClass, list));" +
					" CREATE INDEX " + getTableName() + "_class ON " + getTableName() + "(colClass);" +
					" CREATE INDEX " + getTableName() + "_list ON " + getTableName() + "(list);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colClass, list) VALUES (?, ?)");
			addListStatement = conn.prepareStatement("INSERT INTO list (name, element) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLHasKeyAxiom) {
			OWLHasKeyAxiom naxiom = (OWLHasKeyAxiom) axiom;
			NodeID nid = NodeID.getNodeID();
			if (naxiom.getClassExpression().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getClassExpression()).toString());
				handleAnonymousClassExpression(naxiom.getClassExpression());
			} else {
				addStatement.setString(1, naxiom.getClassExpression().asOWLClass().getIRI().toString());
			}
			addStatement.setString(2, nid.toString());
			
			//for object
			for(OWLObjectPropertyExpression pe : naxiom.getObjectPropertyExpressions()) {
				addListStatement.setString(1, nid.toString());
				if (pe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(pe).toString());
					handleAnonymousObjectPropertyExpression(pe);
				} else {
					addListStatement.setString(2, pe.asOWLObjectProperty().getIRI().toString());
				}
				addListStatement.execute();
			}
			
			//for data
			for(OWLDataPropertyExpression pe : naxiom.getDataPropertyExpressions()) {
				addListStatement.setString(1, nid.toString());
				if (pe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(pe).toString());
					handleAnonymousDataPropertyExpression(pe);
				} else {
					addListStatement.setString(2, pe.asOWLDataProperty().getIRI().toString());
				}
				addListStatement.execute();
			}
			
			return AdditionMode.ADD;
		} else {
			throw new U2R3NotImplementedException();
		}
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
