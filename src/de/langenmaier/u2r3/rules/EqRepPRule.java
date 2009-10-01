package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRepPRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRepPRule.class);
	
	EqRepPRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyAssertion;
		
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.sameAs).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, subjectSourceId, subjectSourceTable, propertySourceId, propertySourceTable, objectSourceId, objectSourceTable)");
			sql.append("\n\t SELECT ass.subject, sa.right, ass.object, MIN(ass.id) AS subjectSourceId, '" + RelationName.propertyAssertion.toString() + "' AS subjectSourceTable, MIN(sa.id) AS propertySourceId, '" + RelationName.sameAs.toString() + "' AS propertySourceTable, MIN(ass.id) AS objectSourceId, '" + RelationName.propertyAssertion.toString() + "' AS objectSourceTable");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT ass.subject, sa.right, ass.object");
		}
		
		if (delta.getRelation() == relationManager.getRelation(RelationName.propertyAssertion)) {
			sql.append("\n\t FROM sameAs AS  sa INNER JOIN " + delta.getDeltaName() + " AS ass ON sa.left = ass.property");
		} else if (delta.getRelation() == relationManager.getRelation(RelationName.sameAs)) {
			sql.append("\n\t FROM " + delta.getDeltaName() + " AS  sa INNER JOIN propertyAssertion AS ass ON sa.left = ass.property");
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, property, object");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = sa.right AND bottom.property = ass.property AND bottom.object=ass.object");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY ass.subject, sa.right, ass.object");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "propertyAssertion(S, R, O) :- sameAs(L, R), propertyAssertion(S, L, O)";
	}

}
