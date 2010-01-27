package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRepPLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRepPLitRule.class);
	
	EqRepPLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.dataPropertyAssertion;
		
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.sameAsEnt).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, language, type, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT ass.subject, sa.right, ass.object, ass.language, ass.type, MIN(ass.id) AS sourceId1, '" + RelationName.dataPropertyAssertion + "' AS sourceTable1, MIN(sa.id) AS sourceId2, '" + RelationName.sameAsEnt + "' AS sourceTable2");
		} else {
			sql.append("(subject, property, object, language, type)");
			sql.append("\n\t SELECT DISTINCT ass.subject, sa.right, ass.object, ass.language, ass.type");
		}

		sql.append("\n\t FROM " + delta.getDeltaName("sameAsEnt") + " AS  sa");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS ass ON sa.colLeft = ass.property");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, property, object");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = ass.subject AND bottom.property = sa.right AND bottom.object=ass.object");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY ass.subject, sa.right, ass.object");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "dataPropertyAssertion(S, R, O) :- sameAsEnt(L, R), dataPropertyAssertion(S, L, O)";
	}

}
