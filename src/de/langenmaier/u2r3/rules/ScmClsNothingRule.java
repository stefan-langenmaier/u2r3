package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmClsNothingRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmClsNothingRule.class);
	
	ScmClsNothingRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, subSourceId, subSourceTable, superSourceId, superSourceTable)");
			sql.append("\n\t SELECT '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "' AS sub, clsA.class AS super, MIN(clsA.id) AS subSourceId, '" + RelationName.classAssertion.toString() + "' AS subSourceTable, MIN(clsA.id) AS superSourceId, '" + RelationName.classAssertion.toString() + "' AS superSourceTable");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "' AS sub, clsA.class AS super");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS clsA");
		sql.append("\n\t WHERE type = '" + OWLXMLVocabulary.CLASS.getURI().toString() + "'");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.sub");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "' AND bottom.super = clsA.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.class");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(nothing, wC) :- classAssertion(C, class)";
	}

}
