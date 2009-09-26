package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmClsSubClassRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmClsSubClassRule.class);
	
	ScmClsSubClassRule() {
		targetRelation = RelationName.subClass;
		
		RelationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		
		RelationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, subSourceId, subSourceTable, superSourceId, superSourceTable)");
			sql.append("\n\t SELECT dec.subject AS sub, dec.subject AS super, MIN(dec.id) AS subSourceId, '" + RelationName.declaration.toString() + "' AS subSourceTable, MIN(dec.id) AS superSourceId, '" + RelationName.declaration.toString() + "' AS superSourceTable");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT dec.subject AS sub, dec.subject AS super");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS dec");
		sql.append("\n\t WHERE type = '" + OWLXMLVocabulary.CLASS.getURI().toString() + "'");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = dec.subject AND bottom.super = dec.subject");
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
