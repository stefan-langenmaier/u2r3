package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsMaxqc2Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsMaxqc2Rule.class);
	
	ClsMaxqc2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
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
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertion") + " AS ca1 ON ca1.type = op.class");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp ON ca1.class = prp.subject AND op.property = prp.property");
		sql.append("\n WHERE mqc.value = '0' AND  oc.class = '" + OWLRDFVocabulary.OWL_THING + "'");	

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- maxQualifiedCardinality(X, 0), onProperty(X, P), onClass(X, thing), classAssertion(U, X), propertyAssertion(U, P, Y)";
	}

}
