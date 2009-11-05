package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmEqp1Sub2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmEqp1Sub2Rule.class);
	
	ScmEqp1Sub2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subProperty;
		
		relationManager.getRelation(RelationName.equivalentProperty).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
		
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT ep.right, ep.left,");
			sql.append(" MIN(ep.id) AS sourceId1, '" + RelationName.equivalentProperty + "' AS sourceTable1");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT ep.right, ep.left ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS ep ");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = ep.left AND bottom.super = ep.right) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ep.left, ep.right");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subProperty(A,B) :- equivalentProperty(A,B)";
	}

}
