package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
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
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colLeft TEXT," +
		" colRight TEXT," +
		" PRIMARY KEY (colLeft, colRight));" +
		" CREATE INDEX " + table + "_left ON " + table + "(colLeft);" +
		" CREATE INDEX " + table + "_right ON " + table + "(colRight);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colLeft, colRight) VALUES (?, ?)";
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDisjointClassesAxiom) {
			OWLDisjointClassesAxiom naxiom = (OWLDisjointClassesAxiom) axiom;
			if (naxiom.getClassExpressions().size() == 2) {
				Iterator<OWLClassExpression> it = naxiom.getClassExpressions().iterator();
				OWLClassExpression ce1 = it.next();
				OWLClassExpression ce2 = it.next();
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
					if (ce1.isAnonymous()) {
						add.setString(1, nidMapper.get(ce1).toString());
					} else {
						add.setString(1, ce1.asOWLClass().getIRI().toString());
					}
					if (ce2.isAnonymous()) {
						add.setString(2, nidMapper.get(ce2).toString());
					} else {
						add.setString(2, ce2.asOWLClass().getIRI().toString());
					}
					
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
				if (ce1.isAnonymous()) {
					handleAddAnonymousClassExpression(ce1);
				}
				
				if (ce2.isAnonymous()) {
					handleAddAnonymousClassExpression(ce2);
				}
				
				return AdditionMode.NOADD;
				
			}
		}
		
		throw new U2R3NotImplementedException();

	}

}
