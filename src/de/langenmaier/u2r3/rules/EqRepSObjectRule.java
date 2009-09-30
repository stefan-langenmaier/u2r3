package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRepSObjectRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRepSObjectRule.class);
	
	EqRepSObjectRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.objectPropertyAssertion;
		
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.sameAs).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

/*		INSERT INTO objectPropertyAssertion_delta (subject, property, object, subjectSourceId, subjectSourceTable, propertySourceId, propertySourceTable, objectSourceId, objectSourceTable)
		SELECT sa.right, ass.property, ass.object, sa.id AS subjectSourceId, 'sameAs' AS subjectSourceTable, ass.id AS propertySourceId, 'objectPropertyAssertion' AS propertySourceTable, ass.id AS objectSourceId, 'objectPropertyAssertion' AS objectSourceTable
		FROM sameAs AS  sa INNER JOIN objectPropertyAssertion AS ass
		  ON sa.left = ass.subject
		WHERE NOT EXISTS (
		SELECT bottom.subject
		FROM objectPropertyAssertion_delta AS bottom
		WHERE bottom.subject = sa.right AND bottom.property = ass.property AND bottom.object=ass.object
		)*/
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT class AS left, class AS right, MIN(id) AS leftSourceId, '" + RelationName.classAssertion.toString() + "' AS leftSourceTable, MIN(id) AS rightSourceId, '" + RelationName.classAssertion.toString() + "' AS rightSourceTable");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT class AS left, subject AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = top.class AND bottom.right = top.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY left, right");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAs(A,A) :- classAssertion(A, X)";
	}

}
