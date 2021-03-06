package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
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
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));
			addListStatement = conn.prepareStatement("INSERT INTO list (name, element) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colClass TEXT," +
		" list TEXT," +
		" PRIMARY KEY (colClass, list));" +
		" CREATE INDEX " + table + "_class ON " + table + "(colClass);" +
		" CREATE INDEX " + table + "_list ON " + table + "(list);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colClass, list) VALUES (?, ?)";
	}

	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLHasKeyAxiom) {
			OWLHasKeyAxiom naxiom = (OWLHasKeyAxiom) axiom;
			NodeID nid = NodeID.getNodeID();
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
				if (naxiom.getClassExpression().isAnonymous()) {
					add.setString(1, nidMapper.get(naxiom.getClassExpression()).toString());
					handleAddAnonymousClassExpression(naxiom.getClassExpression());
				} else {
					add.setString(1, naxiom.getClassExpression().asOWLClass().getIRI().toString());
				}
				add.setString(2, nid.toString());
			}
			
			//for object
			for(OWLObjectPropertyExpression pe : naxiom.getObjectPropertyExpressions()) {
				addListStatement.setString(1, nid.toString());
				if (pe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(pe).toString());
					handleAddAnonymousObjectPropertyExpression(pe);
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
					handleAddAnonymousDataPropertyExpression(pe);
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

}
