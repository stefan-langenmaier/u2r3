package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsMaxqc2EntRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsMaxqc2EntRule.class);
	
	ClsMaxqc2EntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.maxQualifiedCardinality).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.onClass).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("SELECT 1 AS res");
		sql.append("\n FROM " + delta.getDeltaName("maxQualifiedCardinality") + " AS mqc");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON op.class = mqc.class");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("onClass") + " AS oc ON oc.name = mqc.class");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca1 ON ca1.class = op.class");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp ON ca1.entity = prp.subject AND op.property = prp.property");
		sql.append("\n WHERE mqc.value = '0' AND  oc.class = '" + OWLRDFVocabulary.OWL_THING + "'");	

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- maxQualifiedCardinality(X, 0), onProperty(X, P), onClass(X, thing), classAssertionEnt(U, X), objectPropertyAssertion(U, P, Y)";
	}

}
