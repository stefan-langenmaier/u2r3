package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmEqp2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmEqp2Rule.class);
	
	ScmEqp2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.equivalentProperty;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		return applyCollectiveTwice(delta, aux);
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		return applyImmediateTwice(delta, newDelta);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, right, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT sp1.sub, sp2.sub,");
			sql.append(" MIN(sp1.id) AS sourceId1, '" + RelationName.subProperty + "' AS sourceTable1,");
			sql.append(" MIN(sp2.id) AS sourceId2, '" + RelationName.subProperty + "' AS sourceTable2");
		} else {
			sql.append(" (colLeft, right)");
			sql.append("\n\t SELECT DISTINCT sp1.sub, sp2.sub ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("subProperty") + " AS sp1 ");
			sql.append("\n\t\t INNER JOIN subProperty AS sp2 ON sp1.super = sp2.sub AND sp1.sub = sp2.super");
		} else if (run == 1) {
			sql.append("\n\t FROM subProperty AS sp1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp2 ON sp1.super = sp2.sub AND sp1.sub = sp2.super");
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT colLeft, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.colLeft = sp1.sub AND bottom.right = sp2.sub) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sp1.sub, sp2.sub");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "equivalentProperty(A,B) :- subProperty(A,B), subProperty(B,A)";
	}

}
