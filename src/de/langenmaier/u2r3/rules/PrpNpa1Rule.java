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
		
		relationManager.getRelation(RelationName.sourceIndividual).addAdditionRule(this);
		relationManager.getRelation(RelationName.targetIndividual).addAdditionRule(this);
		relationManager.getRelation(RelationName.assertionProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT 1 AS RES");
		sql.append("\n FROM " + delta.getDeltaName("sourceIndividual") +" AS si");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("assertionProperty") +" AS ap ON si.name = ap.name");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("targetIndividual") +" AS ti ON si.name = ti.name");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("propertyAssertion") +" AS prp ON");
		sql.append("\n\t\t prp.subject = si.subject AND prp.property = ap.property AND prp.object = ti.subject");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- sourceIndividual(X, I1), assertionProperty(X, P), targetIndividual(X, I2), propertyAssertion(I1, P, I2)";
	}

}
