package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsNothing2Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsNothing2Rule.class);
	
	ClsNothing2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT subject, type");
		sql.append("\nFROM " + delta.getDeltaName());
		sql.append("\nWHERE type = '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "'");
		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- declaration(A, nothing)";
	}

}
