package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpDomLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpDomLitRule.class);
	
	PrpDomLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		//relations on the right side
		relationManager.getRelation(RelationName.propertyDomain).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT ass.subject, dom.Domain, MIN(ass.id) AS sourceId1, '" + RelationName.dataPropertyAssertion + "' AS sourceTable1, MIN(dom.id) AS sourceId2, '" + RelationName.propertyDomain + "' AS sourceTable2");
		} else {
			sql.append("(entity, colClass)");
			sql.append("\n\t SELECT DISTINCT ass.subject, dom.Domain");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("dataPropertyAssertion") + " AS ass");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyDomain") + " AS dom");
		sql.append("\n\t\t ON ass.Property = dom.Property");

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT entity, colClass");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = ass.subject AND bottom.colClass = dom.domain");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ass.subject, dom.Domain");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(X, C) :- propertyDomain(P, C), dataPropertyAssertion(X, P, Y)";
	}

}
