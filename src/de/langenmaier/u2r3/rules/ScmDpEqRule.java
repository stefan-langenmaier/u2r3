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
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
		
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, right, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT ca.entity, ca.entity, ");
			sql.append(" MIN(ca.id) AS sourceId1, '" + RelationName.classAssertionEnt + "' AS sourceTable1");
		} else {
			sql.append(" (colLeft, right)");
			sql.append("\n\t SELECT DISTINCT ca.entity, ca.entity ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertionEnt") + " AS ca ");
		sql.append("\n\t WHERE ca.class = '" + OWLRDFVocabulary.OWL_DATA_PROPERTY + "'");
		
		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT colLeft, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.colLeft = ca.entity AND bottom.right = ca.entity) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ca.entity");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "equivalentProperty(A,A) :- classAssertionEnt(A, dataProp)";
	}

}
