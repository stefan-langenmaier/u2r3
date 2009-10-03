package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpSpo1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpSpo1Rule.class);
	
	PrpSpo1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
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
			sql.append("\n\t SELECT prp.subject AS subject, subP.super AS property, prp.object AS object, MIN(prp.id) AS subjectSourceId, '" + RelationName.propertyAssertion + "' AS subjectSourceTable, MIN(subP.id) AS propertySourceId, '" + RelationName.subProperty + "' AS propertySourceTable, MIN(prp.id) AS objectSourceId, '" + RelationName.propertyAssertion + "' AS objectSourceTable");
		} else {
			sql.append("(subject, type)");
			sql.append("\n\t SELECT DISTINCT prp.subject AS subject, subP.super AS property, prp.object AS object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("subProperty") + " AS subP");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp ON subP.sub = prp.property");

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = prp.subject AND bottom.property = subP.super AND bottom.object = prp.object");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.subject, subP.super, prp.object");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "propertyAssertion(Y, P2, X) :- subProperty(P, P2), propertyAssertion(X, P1, Y)";
	}

}
