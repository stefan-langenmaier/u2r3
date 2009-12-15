package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class PropertyChainRelation extends Relation {
	
	protected PropertyChainRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyChain";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" property TEXT," +
					" list TEXT," +
					" PRIMARY KEY (property, list))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (property, list) VALUES (?, ?)");
			addListStatement = conn.prepareStatement("INSERT INTO list (name, element) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		OWLSubPropertyChainOfAxiom pc = (OWLSubPropertyChainOfAxiom) axiom;
		
		try {
			NodeID nid = NodeID.getNodeID();
			if (pc.getSuperProperty().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(pc.getSuperProperty()).toString());
			} else {
				addStatement.setString(1, pc.getSuperProperty().asOWLObjectProperty().getIRI().toString());
			}
			addStatement.setString(2, nid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			for (OWLObjectPropertyExpression npe : pc.getPropertyChain()) {
				
				addListStatement.setString(1, nid.toString());
				if (npe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(npe).toString());
				} else {
					addListStatement.setString(2, npe.asOWLObjectProperty().getIRI().toString());
				}
				addListStatement.execute();
				
				if (npe.isAnonymous()) {
					handleAnonymousObjectPropertyExpression(npe);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return AdditionMode.NOADD;
		
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
