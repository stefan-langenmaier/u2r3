package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpRngObjectRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpRngObjectRule.class);
	
	PrpRngObjectRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.declaration;
		
		//relations on the right side
		relationManager.getRelation(RelationName.objectPropertyRange).addAdditionRule(this);
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
			sql.append(" (subject, type, subjectSourceId, subjectSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT ass.object, rng.range, MIN(ass.id) AS subjectSourceId, '" + RelationName.objectPropertyAssertion + "' AS subjectSourceTable, MIN(rng.id) AS typeSourceId, '" + RelationName.objectPropertyDomain + "' AS typeSourceTable");
		} else {
			sql.append("(subject, type)");
			sql.append("\n\t SELECT DISTINCT ass.object, rng.range");
		}
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM objectPropertyAssertion AS ass");
			sql.append("\n\t\t INNER JOIN objectPropertyRange AS rng");
		} else {
			if (relationManager.getRelation(RelationName.objectPropertyAssertion) == delta.getRelation()) {
				sql.append("\n\t FROM " + delta.getDeltaName() + " AS ass");
				sql.append("\n\t\t INNER JOIN objectPropertyRange AS rng");
			} else {
				sql.append("\n\t FROM objectPropertyAssertion AS ass");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS rng");
			}
		}
		sql.append("\n\t\t ON ass.Property = rng.Property");

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = ass.object AND bottom.type = rng.range");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ass.object, rng.range");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "declaration(Y, C) :- objectPropertyDomain(P, C), objectPropertyAssertion(X, P, Y)";
	}

}
