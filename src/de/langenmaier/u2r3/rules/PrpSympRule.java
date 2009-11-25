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
		targetRelation = RelationName.objectPropertyAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT prp.object AS subject, prp.property AS property, prp.subject AS object, ");
			sql.append(" MIN(prp.id) AS sourceId1, '" + RelationName.objectPropertyAssertion + "' AS sourceTable1, ");
			sql.append(" MIN(clsA.id) AS sourceId2, '" + RelationName.classAssertionEnt + "' AS sourceTable2");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT prp.object AS subject, prp.property AS property, prp.subject AS object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp ON clsA.entity = prp.property");

		sql.append("\n\t WHERE clsA.class = '" + OWLRDFVocabulary.OWL_SYMMETRIC_PROPERTY + "'");

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
		return "objectPropertyAssertion(Y, P, X) :- classAssertionEnt(P, 'symmetric'), objectPropertyAssertion(X, P, Y)";
	}

}
