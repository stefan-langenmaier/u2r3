package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmClsEquivalentClassRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmClsEquivalentClassRule.class);
	
	ScmClsEquivalentClassRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.equivalentClass;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT clsA.class AS left, clsA.class AS right, MIN(clsA.id) AS leftSourceId, '" + RelationName.classAssertion.toString() + "' AS leftSourceTable, MIN(clsA.id) AS rightSourceId, '" + RelationName.classAssertion.toString() + "' AS rightSourceTable");
		} else {
			sql.append(" (left, right)");
			sql.append("\n\t SELECT DISTINCT clsA.class AS left, clsA.class AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS clsA");
		sql.append("\n\t WHERE type = '" + OWLXMLVocabulary.CLASS.getURI().toString() + "'");
		
		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = clsA.class AND bottom.right = clsA.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.class");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "equivalentClass(C, C) :- classAssertion(C, class)";
	}

}
