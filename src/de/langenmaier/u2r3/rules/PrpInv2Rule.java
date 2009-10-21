package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpInv2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpInv2Rule.class);
	
	PrpInv2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.objectPropertyAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.inverseOf).addAdditionRule(this);
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
			sql.append(" (subject, property, object, sourceId1, sourceTable2, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT prp.object AS subject, inv.left AS property, prp.subject AS object, ");
			sql.append(" MIN(prp.id) AS sourceId1, '" + RelationName.objectPropertyAssertion + "' AS sourceTable1, ");
			sql.append(" MIN(inv.id) AS sourceId2, '" + RelationName.inverseOf + "' AS sourceTable2");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT prp.object AS subject, inv.left AS property, prp.subject AS object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("inverseOf") + " AS inv");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp ON inv.right = prp.property");

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = prp.object AND bottom.property = inv.left AND bottom.object = prp.subject");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.object, inv.left, prp.subject");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "objectPropertyAssertion(Y, P1, X) :- inverseOf(P1, P2), objectPropertyAssertion(X, P2, Y)";
	}

}
