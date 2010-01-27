package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsMaxqc1LitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsMaxqc1LitRule.class);
	
	ClsMaxqc1LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.maxQualifiedCardinality).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT 1 AS res");
		sql.append("\n FROM " + delta.getDeltaName("maxQualifiedCardinality") + " AS mqc");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca1 ON ca1.colClass = mqc.colClass");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp ON ca1.entity = prp.subject AND mqc.property = prp.property");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionLit") + " AS ca2 ON ca2.literal = prp.object AND ca2.colClass = mqc.total");
		sql.append("\n WHERE mqc.value = '0'");	

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- maxQualifiedCardinality(X, 0), onProperty(X, P), onClass(X, C), classAssertionEnt(U, X), dataPropertyAssertion(U, P, Y), classAssertionlit(Y, C)";
	}

}
