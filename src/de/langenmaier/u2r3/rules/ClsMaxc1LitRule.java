package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsMaxc1LitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsMaxc1LitRule.class);
	
	ClsMaxc1LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.maxCardinality).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT 1 AS res");
		sql.append("\n FROM " + delta.getDeltaName("maxCardinality") + " AS mc");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON op.class = mc.class");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca ON ca.class = op.class");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp ON ca.literal = prp.subject AND op.property = prp.property");
		sql.append("\n WHERE mc.value = '0'");	

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- maxCardinality(X, 0), onProperty(X, P), classAssertionEnt(U, X), dataPropertyAssertion(U, P, Y)";
	}

}
