package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class DisjointWithRelation extends Relation {
	static Logger logger = Logger.getLogger(DisjointWithRelation.class);
	
	protected DisjointWithRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "disjointWith";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					"id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left VARCHAR(100)," +
					" right VARCHAR(100)," +
					" PRIMARY KEY (left, right))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDisjointClassesAxiom) {
			OWLDisjointClassesAxiom naxiom = (OWLDisjointClassesAxiom) axiom;
			for (OWLClassExpression ce1 : naxiom.getClassExpressions()) {
				for (OWLClassExpression ce2 : naxiom.getClassExpressions()) {
					if (!ce1.equals(ce2)) {
						if (ce1.isAnonymous()) {
							addStatement.setString(1, nidMapper.get(ce1).toString());
						} else {
							addStatement.setString(1, ce1.asOWLClass().getIRI().toString());
						}
						if (ce2.isAnonymous()) {
							addStatement.setString(2, nidMapper.get(ce2).toString());
						} else {
							addStatement.setString(2, ce2.asOWLClass().getIRI().toString());
						}
						
						addStatement.execute();
						reasonProcessor.add(new AdditionReason(this));
					}
				}
				if (ce1.isAnonymous()) {
					handleAnonymousClassExpression(ce1);
				}
			}

			return false;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (" +
					"id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" left VARCHAR(100)," +
					" right VARCHAR(100)," +
					" leftSourceId UUID," +
					" leftSourceTable VARCHAR(100)," +
					" rightSourceId UUID," +
					" rightSourceTable VARCHAR(100)," +
					" PRIMARY KEY (left, right))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void merge(DeltaRelation delta) {
		
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
				return null;
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}


}
