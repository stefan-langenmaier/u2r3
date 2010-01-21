package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class DisjointWithRelation extends Relation {
	static Logger logger = Logger.getLogger(DisjointWithRelation.class);
	
	protected DisjointWithRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "disjointWith";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" left TEXT," +
					" right TEXT," +
					" PRIMARY KEY (left, right));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(left);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(right);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDisjointClassesAxiom) {
			OWLDisjointClassesAxiom naxiom = (OWLDisjointClassesAxiom) axiom;
			if (naxiom.getClassExpressions().size() == 2) {
				Iterator<OWLClassExpression> it = naxiom.getClassExpressions().iterator();
				OWLClassExpression ce1 = it.next();
				OWLClassExpression ce2 = it.next();
				
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
				
				if (ce1.isAnonymous()) {
					handleAnonymousClassExpression(ce1);
				}
				
				if (ce2.isAnonymous()) {
					handleAnonymousClassExpression(ce2);
				}
				
				return AdditionMode.NOADD;
				
			}
		}
		
		throw new U2R3NotImplementedException();

	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}
	
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
