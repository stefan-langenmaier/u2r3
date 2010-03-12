package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.AdditionReason;

public class UnionOfRelation extends Relation {
	
	protected UnionOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "unionOf";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colClass, list) VALUES (?, ?)");
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
	public void add(OWLObject ce) {
		OWLObjectUnionOf ouo = (OWLObjectUnionOf) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
				addStatement.setString(1, nidMapper.get(ce).toString());
				addStatement.setString(2, nid.toString());
				addStatement.execute();
			}
			if (reasoner.isAdditionMode()) {
				reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
			} else {
				reasonProcessor.add(new AdditionReason(this));
			}
			
			for (OWLClassExpression nce : ouo.getOperands()) {
				addListStatement.setString(1, nid.toString());
				if (nce.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(nce).toString());
				} else {
					addListStatement.setString(2, nce.asOWLClass().getIRI().toString());
				}
				
				addListStatement.execute();
				if (nce.isAnonymous()) {
					handleAddAnonymousClassExpression(nce);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
