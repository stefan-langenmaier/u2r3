package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;

public class PrpIrpRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpIrpRule.class);
	
	PrpIrpRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
		//RelationManager.getRelation(RelationName.sameAs).addDeletionRule(this);
	}
	


	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		if (delta.getRelation() == relationManager.getRelation(RelationName.declaration)) {
			sql.append("\nFROM " + delta.getDeltaName() + " AS dec INNER JOIN objectPropertyAssertion AS prp ON dec.subject = prp.property");
		} else if (delta.getRelation() == relationManager.getRelation(RelationName.objectPropertyAssertion)) {
			sql.append("\nFROM declaration AS dec INNER JOIN " + delta.getDeltaName() + " AS prp ON dec.subject = prp.property");
		}
		sql.append("\nWHERE type = '" + OWLRDFVocabulary.OWL_IRREFLEXIVE_PROPERTY.getIRI().toString() + "'");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- declaration(A, irreflexiv), objectPropertyAssertion(X, A, X)";
	}

}
