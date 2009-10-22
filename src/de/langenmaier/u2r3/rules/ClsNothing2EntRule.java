package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsNothing2EntRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsNothing2EntRule.class);
	
	ClsNothing2EntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT entity");
		sql.append("\nFROM " + delta.getDeltaName("classAssertionEnt"));
		sql.append("\nWHERE class = '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "'");
		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertionEnt(A, nothing)";
	}

}
