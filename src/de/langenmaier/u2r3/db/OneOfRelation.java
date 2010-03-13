package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectOneOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.AdditionReason;

public class OneOfRelation extends Relation {
	
	protected OneOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "oneOf";
			
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
	public void add(OWLObject ce) {
		OWLObjectOneOf oo = (OWLObjectOneOf) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
				add.setString(1, nidMapper.get(ce).toString());
				add.setString(2, nid.toString());
				add.execute();
			}
			if (reasoner.isAdditionMode()) {
				reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
			} else {
				reasonProcessor.add(new AdditionReason(this));
			}
			
			for (OWLIndividual ind : oo.getIndividuals()) {
				addListStatement.setString(1, nid.toString());
				if (ind.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(ind).toString());
				} else {
					addListStatement.setString(2, ind.asOWLNamedIndividual().getIRI().toString());
				}
				
				addListStatement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
