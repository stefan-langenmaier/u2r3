package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmDom2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmDom2Rule.class);
	
	ScmDom2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyDomain;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyDomain).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (property, domain, propertySourceId, propertySourceTable, domainSourceId, domainSourceTable)");
			sql.append("\n\t SELECT sp.sub, dom.domain, MIN(sp.id) AS propertySourceId, '" + RelationName.subProperty + "' AS propertySourceTable, MIN(dom.id) AS domainSourceId, '" + RelationName.propertyDomain + "' AS domainSourceTable");
		} else {
			sql.append(" (property, domain)");
			sql.append("\n\t SELECT DISTINCT sp.sub, dom.domain ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("propertyDomain") + " AS dom ");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.super = dom.property");
		
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT property, domain");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.property = sp.sub AND bottom.domain = dom.domain) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sp.sub, dom.domain");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "domain(P2,C) :- subProperty(P1,P2), domain(P1,C)";
	}

}
