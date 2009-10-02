package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpSympRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpSympRule.class);
	
	PrpSympRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyAssertion;
		
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
			sql.append(" (subject, property, object, subjectSourceId, subjectSourceTable, propertySourceId, propertySourceTable, objectSourceId, objectSourceTable)");
			sql.append("\n\t SELECT prp.object AS subject, prp.property AS property, prp.subject AS object, MIN(prp.id) AS subjectSourceId, '" + RelationName.propertyAssertion + "' AS subjectSourceTable, MIN(prp.id) AS propertySourceId, '" + RelationName.propertyAssertion + "' AS propertySourceTable, MIN(prp.id) AS objectSourceId, '" + RelationName.propertyAssertion + "' AS objectSourceTable");
		} else {
			sql.append("(subject, type)");
			sql.append("\n\t SELECT DISTINCT prp.object AS subject, prp.property AS property, prp.subject AS object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertion") + " AS clsA");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp ON clsA.class = prp.property");

		sql.append("\n\t WHERE clsA.type = '" + OWLRDFVocabulary.OWL_SYMMETRIC_PROPERTY + "'");

		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = prp.object AND bottom.property = prp.property AND bottom.object = prp.subject");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.object, prp.property, prp.subject");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "propertyAssertion(Y, P, X) :- classAssertion(P, 'symmetric'), propertyAssertion(X, P, Y)";
	}

}
