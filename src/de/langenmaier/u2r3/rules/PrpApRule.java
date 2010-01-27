package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;

public class PrpApRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpApRule.class);
	
	PrpApRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		String datatype = OWLRDFVocabulary.OWL_ANNOTATION_PROPERTY.getURI().toString();
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		sql.append(" (entity, colClass) VALUES ");

		sql.append("\n ('" + OWLRDFVocabulary.OWL_BACKWARD_COMPATIBLE_WITH.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.RDFS_COMMENT.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.OWL_DEPRECATED.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.OWL_INCOMPATIBLE_WITH.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.RDFS_IS_DEFINED_BY.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.RDFS_LABEL.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.OWL_PRIOR_VERSION.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.RDFS_SEE_ALSO.getIRI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.OWL_VERSION_INFO.getIRI().toString() + "', '" + datatype + "')");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(ap, annotationProperty) :- true";
	}

}
