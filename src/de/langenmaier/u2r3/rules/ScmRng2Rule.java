package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmRng2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmRng2Rule.class);
	
	ScmRng2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyRange;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyRange).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (property, range, propertySourceId, propertySourceTable, rangeSourceId, rangeSourceTable)");
			sql.append("\n\t SELECT sp.sub, rng.range, MIN(sp.id) AS propertySourceId, '" + RelationName.subProperty + "' AS propertySourceTable, MIN(rng.id) AS rangeSourceId, '" + RelationName.propertyRange + "' AS rangeSourceTable");
		} else {
			sql.append(" (property, range)");
			sql.append("\n\t SELECT DISTINCT sp.sub, rng.range ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("propertyRange") + " AS rng ");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.super = rng.property");
		
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT property, range");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.property = sp.sub AND bottom.range = rng.range) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sp.sub, rng.range");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "range(P2,C) :- subProperty(P1,P2), range(P1,C)";
	}

}
