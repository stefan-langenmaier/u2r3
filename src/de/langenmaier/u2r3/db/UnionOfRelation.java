package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class UnionOfRelation extends Relation {
	
	protected UnionOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "unionOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" class TEXT," +
					" list TEXT," +
					" PRIMARY KEY (class, list));" +
					" CREATE HASH INDEX " + getTableName() + "_class ON " + getTableName() + "(class);" +
					" CREATE HASH INDEX " + getTableName() + "_list ON " + getTableName() + "(list);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, list) VALUES (?, ?)");
			addListStatement = conn.prepareStatement("INSERT INTO list (name, element) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void add(OWLObject ce) {
		OWLObjectUnionOf ouo = (OWLObjectUnionOf) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			addStatement.setString(1, nidMapper.get(ce).toString());
			addStatement.setString(2, nid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			for (OWLClassExpression nce : ouo.getOperands()) {
				addListStatement.setString(1, nid.toString());
				if (nce.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(nce).toString());
				} else {
					addListStatement.setString(2, nce.asOWLClass().getIRI().toString());
				}
				
				addListStatement.execute();
				if (nce.isAnonymous()) {
					handleAnonymousClassExpression(nce);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
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

	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();
	}

}
