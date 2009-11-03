package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectOneOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class OneOfRelation extends Relation {
	
	protected OneOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "oneOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					"id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" class TEXT," +
					" list TEXT," +
					" PRIMARY KEY (class, list))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, list) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();

	}
	
	@Override
	public void add(OWLObject ce) {
		OWLObjectOneOf oo = (OWLObjectOneOf) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			int ordnung = 0;
			addStatement.setString(1, nidMapper.get(ce).toString());
			addStatement.setString(2, nid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			for (OWLIndividual ind : oo.getIndividuals()) {
				addListStatement.setString(1, nid.toString());
				if (ind.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(ind).toString());
				} else {
					addListStatement.setString(2, ind.asNamedIndividual().getIRI().toString());
				}
				addListStatement.setLong(3, ++ordnung);
				
				addListStatement.execute();
				if (ind.isAnonymous()) {
					handleAnonymousIndividual(ind);
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
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
