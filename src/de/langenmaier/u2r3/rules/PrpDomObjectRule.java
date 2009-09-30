package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpDomObjectRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpDomObjectRule.class);
	
	PrpDomObjectRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.objectPropertyDomain).addAdditionRule(this);
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
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT ass.subject, dom.Domain, MIN(ass.id) AS classSourceId, '" + RelationName.objectPropertyAssertion + "' AS classSourceTable, MIN(dom.id) AS typeSourceId, '" + RelationName.objectPropertyDomain + "' AS typeSourceTable");
		} else {
			sql.append("(class, type)");
			sql.append("\n\t SELECT DISTINCT ass.subject, dom.Domain");
		}
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM objectPropertyAssertion AS ass");
			sql.append("\n\t\t INNER JOIN objectPropertyDomain AS dom");
		} else {
			if (relationManager.getRelation(RelationName.objectPropertyAssertion) == delta.getRelation()) {
				sql.append("\n\t FROM " + delta.getDeltaName() + " AS ass");
				sql.append("\n\t\t INNER JOIN objectPropertyDomain AS dom");
			} else {
				sql.append("\n\t FROM objectPropertyAssertion AS ass");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS dom");
			}
		}
		sql.append("\n\t\t ON ass.Property = dom.Property");

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT class, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = ass.subject AND bottom.type = dom.domain");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ass.subject, dom.Domain");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(X, C) :- objectPropertyDomain(P, C), objectPropertyAssertion(X, P, Y)";
	}

}
