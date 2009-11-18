package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class PrpNpa1Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpNpa1Rule.class);
	
	PrpNpa1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.negativeObjectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT 1 AS RES");
		sql.append("\n FROM " + delta.getDeltaName("negativeObjectPropertyAssertion") +" AS nopa");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") +" AS prp ON");
		sql.append("\n\t\t prp.subject = nopa.subject AND prp.property = nopa.property AND prp.object = nopa.object");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- sourceIndividual(X, I1), assertionProperty(X, P), targetIndividual(X, I2), objectPropertyAssertion(I1, P, I2)";
	}

}
