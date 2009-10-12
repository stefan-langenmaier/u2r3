package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmOpSubRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmOpSubRule.class);
	
	ScmOpSubRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subProperty;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
		
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, subSourceId, subSourceTable, superSourceId, superSourceTable)");
			sql.append("\n\t SELECT ca.class, ca.class, MIN(ca.id) AS subSourceId, '" + RelationName.classAssertion + "' AS subSourceTable, MIN(ca.id) AS superSourceId, '" + RelationName.classAssertion + "' AS superSourceTable");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT ca.class, ca.class ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS ca ");
		sql.append("\n\t WHERE ca.type = '" + OWLRDFVocabulary.OWL_OBJECT_PROPERTY + "'");
		
		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = ca.class AND bottom.super = ca.class) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ca.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subProperty(A,A) :- classAssertion(A,objProp)";
	}

}
