package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsNothing2LitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsNothing2LitRule.class);
	
	ClsNothing2LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT literal");
		sql.append("\nFROM " + delta.getDeltaName("classAssertionLit"));
		sql.append("\nWHERE colClass = '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "'");
		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertionLit(A, nothing)";
	}

}
