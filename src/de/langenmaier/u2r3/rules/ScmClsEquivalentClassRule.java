package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmClsEquivalentClassRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmClsEquivalentClassRule.class);
	
	ScmClsEquivalentClassRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.equivalentClass;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT dec.subject AS left, dec.subject AS right, MIN(dec.id) AS leftSourceId, '" + RelationName.declaration.toString() + "' AS leftSourceTable, MIN(dec.id) AS rightSourceId, '" + RelationName.declaration.toString() + "' AS rightSourceTable");
		} else {
			sql.append(" (left, right)");
			sql.append("\n\t SELECT DISTINCT dec.subject AS left, dec.subject AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS dec");
		sql.append("\n\t WHERE type = '" + OWLXMLVocabulary.CLASS.getURI().toString() + "'");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = dec.subject AND bottom.right = dec.subject");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY dec.subject");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C,C) :- declaration(C, class)";
	}

}
