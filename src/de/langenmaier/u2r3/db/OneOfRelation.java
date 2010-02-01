package de.langenmaier.u2r3.db;

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
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
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
	public void add(OWLObject ce) {
		OWLObjectOneOf oo = (OWLObjectOneOf) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			addStatement.setString(1, nidMapper.get(ce).toString());
			addStatement.setString(2, nid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
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
