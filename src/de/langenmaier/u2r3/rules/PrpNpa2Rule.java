package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class PrpNpa2Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(PrpNpa2Rule.class);
	
	PrpNpa2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.negativeDataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT 1 AS RES");
		sql.append("\n FROM " + delta.getDeltaName("negativeDataPropertyAssertion") +" AS ndpa");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") +" AS prp ON");
		sql.append("\n\t\t prp.subject = ndpa.subject AND prp.property = ndpa.property AND prp.object = ndpa.object");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- sourceIndividual(X, I1), assertionProperty(X, P), targetValue(X, I2), dataPropertyAssertion(I1, P, I2)";
	}

}