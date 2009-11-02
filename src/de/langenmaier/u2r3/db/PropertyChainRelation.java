package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class PropertyChainRelation extends Relation {
	
	protected PropertyChainRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyChain";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, property VARCHAR(100), list VARCHAR(100), PRIMARY KEY (property, list))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (property, list) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		OWLSubPropertyChainOfAxiom pc = (OWLSubPropertyChainOfAxiom) axiom;
		
		try {
			NodeID nid = NodeID.getNodeID();
			int ordnung = 0;
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
				addListStatement.setLong(3, ++ordnung);
				addListStatement.execute();
				
				if (npe.isAnonymous()) {
					handleAnonymousObjectPropertyExpression(npe);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
		
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" property VARCHAR(100)," +
					" list VARCHAR(100)," +
					" propertySourceId UUID," +
					" propertySourceTable VARCHAR(100)," +
					" listSourceId UUID," +
					" listSourceTable VARCHAR(100)," +
					" PRIMARY KEY (class, list))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

}
