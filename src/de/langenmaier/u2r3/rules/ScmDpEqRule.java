package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmDpEqRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmDpEqRule.class);
	
	ScmDpEqRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.equivalentProperty;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
		
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT ca.class, ca.class, MIN(ca.id) AS subSourceId, '" + RelationName.classAssertion + "' AS subSourceTable, MIN(ca.id) AS superSourceId, '" + RelationName.classAssertion + "' AS superSourceTable");
		} else {
			sql.append(" (left, right)");
			sql.append("\n\t SELECT DISTINCT ca.class, ca.class ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS ca ");
		sql.append("\n\t WHERE ca.type = '" + OWLRDFVocabulary.OWL_DATA_PROPERTY + "'");
		
		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = ca.class AND bottom.right = ca.class) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ca.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "equivalentProperty(A,A) :- classAssertion(A, dataProp)";
	}

}
