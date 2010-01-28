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
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		sql.append("\nFROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp ON clsA.entity = prp.property");

		sql.append("\nWHERE clsA.colClass = '" + OWLRDFVocabulary.OWL_IRREFLEXIVE_PROPERTY.getIRI().toString() + "'");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertion(A, irreflexiv), objectPropertyAssertion(X, A, X)";
	}

}