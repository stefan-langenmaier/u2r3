package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmDom1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmDom1Rule.class);
	
	ScmDom1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyDomain;
		
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyDomain).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (property, domain, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT dom.property, sc.super,");
			sql.append(" MIN(dom.id) AS sourceId1, '" + RelationName.propertyDomain + "' AS sourceTable,");
			sql.append(" MIN(sc.id) AS sourceId2, '" + RelationName.subClass + "' AS sourceTable");
		} else {
			sql.append(" (property, domain)");
			sql.append("\n\t SELECT DISTINCT dom.property, sc.super ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("propertyDomain") + " AS dom ");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subClass") + " AS sc ON sc.sub = dom.domain");
		
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT property, domain");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.property = dom.property AND bottom.domain = sc.super) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY dom.property, sc.super");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "domain(P, C2) :- subClass(C1, C2), domain(P, C1)";
	}

}
