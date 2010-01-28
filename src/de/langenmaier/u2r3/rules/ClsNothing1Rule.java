package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;

public class ClsNothing1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsNothing1Rule.class);
	
	ClsNothing1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		String datatype = OWLRDFVocabulary.OWL_CLASS.getIRI().toString();
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		sql.append(" (entity, colClass) VALUES ");

		sql.append("\n ('" + OWLRDFVocabulary.OWL_NOTHING + "', '" + datatype + "')");
	
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(nothing, class) :- true";
	}

}
