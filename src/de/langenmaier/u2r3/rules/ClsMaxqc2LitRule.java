package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsMaxqc2LitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsMaxqc2LitRule.class);
	
	ClsMaxqc2LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
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
		sql.append("\n WHERE mqc.value = '0' AND  mqc.total = '" + OWLRDFVocabulary.OWL_THING + "'");	

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- maxQualifiedCardinality(X, 0), onProperty(X, P), onClass(X, thing), classAssertionEnt(U, X), dataPropertyAssertion(U, P, Y)";
	}

}
