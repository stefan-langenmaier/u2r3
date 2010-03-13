package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.AdditionReason;

public class PropertyChainRelation extends Relation {
	
	protected PropertyChainRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyChain";
			
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
		" property TEXT," +
		" list TEXT," +
		" PRIMARY KEY (property, list));" +
		" CREATE INDEX " + table + "_property ON " + table + "(property);" +
		" CREATE INDEX " + table + "_list ON " + table + "(list);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (property, list) VALUES (?, ?)";
	}

	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		OWLSubPropertyChainOfAxiom pc = (OWLSubPropertyChainOfAxiom) axiom;
		
		try {
			NodeID nid = NodeID.getNodeID();
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
				if (pc.getSuperProperty().isAnonymous()) {
					add.setString(1, nidMapper.get(pc.getSuperProperty()).toString());
				} else {
					add.setString(1, pc.getSuperProperty().asOWLObjectProperty().getIRI().toString());
				}
				add.setString(2, nid.toString());
				add.execute();
			}
			if (reasoner.isAdditionMode()) {
				reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
			} else {
				reasonProcessor.add(new AdditionReason(this));
			}
			
			for (OWLObjectPropertyExpression npe : pc.getPropertyChain()) {
				
				addListStatement.setString(1, nid.toString());
				if (npe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(npe).toString());
				} else {
					addListStatement.setString(2, npe.asOWLObjectProperty().getIRI().toString());
				}
				addListStatement.execute();
				
				if (npe.isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(npe);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return AdditionMode.NOADD;
		
	}

}
