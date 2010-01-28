package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRepSEntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRepSEntRule.class);
	
	EqRepSEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.objectPropertyAssertion;
		
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.sameAsEnt).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT sa.colRight, ass.property, ass.object, MIN(sa.id) AS sourceId1, '" + RelationName.sameAsEnt + "' AS sourceTable1, MIN(ass.id) AS sourceId2, '" + RelationName.objectPropertyAssertion + "' AS sourceTable2");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT sa.colRight, ass.property, ass.object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("sameAsEnt") + " AS  sa");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS ass ON sa.colLeft = ass.subject");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, property, object");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = sa.colRight AND bottom.property = ass.property AND bottom.object=ass.object");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY sa.colRight, ass.property, ass.object");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "objectPropertyAssertion(R, P, O) :- sameAsEnt(L, R), objectPropertyAssertion(L, P, O)";
	}

}
