package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpFpRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpFpRule.class);
	
	PrpFpRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAs;
		
		//relations on the right side
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT prp1.object AS left, prp2.object AS right, MIN(prp1.id) AS leftSourceId, '" + RelationName.propertyAssertion + "' AS leftSourceTable, MIN(prp2.id) AS rightSourceId, '" + RelationName.propertyAssertion + "' AS rightSourceTable");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT prp1.object AS left, prp2.object AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertion") + " AS clsA");
		if (run == 0) {
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp1 ON clsA.class = prp1.property");
			sql.append("\n\t\t INNER JOIN propertyAssertion AS prp2 ON clsA.class = prp2.property AND prp1.subject = prp2.subject");
		} else if (run == 1) {
			sql.append("\n\t\t INNER JOIN propertyAssertion AS prp1 ON clsA.class = prp1.property");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp2 ON clsA.class = prp2.property AND prp1.property = prp2.subject");
		}
		sql.append("\n\t WHERE clsA.type = '" + OWLRDFVocabulary.OWL_FUNCTIONAL_PROPERTY + "'");

		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.left");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = prp1.object AND bottom.right = prp2.object");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp1.object, prp2.object");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAs(Y1, Y2) :- classAssertion(P, 'functional'), propertyAssertion(X, P, Y1), propertyAssertion(X, P, Y2)";
	}

}
