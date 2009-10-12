package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmRng1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmRng1Rule.class);
	
	ScmRng1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyRange;
		
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyRange).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (property, range, propertySourceId, propertySourceTable, rangeSourceId, rangeSourceTable)");
			sql.append("\n\t SELECT rng.property, sc.super, MIN(rng.id) AS propertySourceId, '" + RelationName.propertyRange + "' AS propertySourceTable, MIN(sc.id) AS domainSourceId, '" + RelationName.subClass + "' AS domainSourceTable");
		} else {
			sql.append(" (property, range)");
			sql.append("\n\t SELECT DISTINCT rng.property, sc.super ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("propertyRange") + " AS rng ");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subClass") + " AS sc ON sc.sub = rng.range");
		
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT property, range");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.property = rng.property AND bottom.range = sc.super) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY rng.property, sc.super");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "range(P, C2) :- subClass(C1, C2), range(P, C1)";
	}

}
