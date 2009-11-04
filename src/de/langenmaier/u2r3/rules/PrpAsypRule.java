package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class PrpAsypRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpAsypRule.class);
	
	PrpAsypRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		return applyCollectiveTwice(delta, aux);
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		return applyImmediateTwice(delta, newDelta);
	}


	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
	 	
		sql.append("SELECT '1' AS res");
		sql.append("\nFROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp1 ON clsA.entity = prp1.property");
			sql.append("\n\t INNER JOIN objectPropertyAssertion AS prp2 ON clsA.entity = prp2.property");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN objectPropertyAssertion AS prp1 ON clsA.entity = prp1.property");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp2 ON clsA.entity = prp2.property");
		}
		sql.append("\n\t\t AND prp1.subject = prp2.object AND prp1.object = prp2.subject");
		sql.append("\nWHERE clsA.class = '" + OWLRDFVocabulary.OWL_ASYMMETRIC_PROPERTY + "'");
		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertionEnt(P, asymmetric), objectPropertyAssertion(X, P, Y), objectPropertyAssertion(Y, P, X)";
	}

}
