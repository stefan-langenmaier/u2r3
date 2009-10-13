package de.langenmaier.u2r3.rules;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmHvRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmHvRule.class);
	
	ScmHvRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.subProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.hasValue).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		String sql = null;
		try {
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet		
				sql = buildQuery(delta, aux, true, 0);
				logger.trace("Adding delta data (NO_DELTA): " + sql);
				rows = statement.executeUpdate(sql);

			} else {
				sql = buildQuery(delta, aux, true, 0);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
				rows = statement.executeUpdate(sql);

				sql = buildQuery(delta, aux, true, 1);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 1): " + sql);
				rows = statement.executeUpdate(sql);
	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		String sql = null;
		try {
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet		
				sql = buildQuery(delta, newDelta, false, 0);
				logger.trace("Adding delta data (NO_DELTA): " + sql);
				rows = statement.executeUpdate(sql);
	
			} else {
				sql = buildQuery(delta, newDelta, false, 0);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
				rows = statement.executeUpdate(sql);
				
				sql = buildQuery(delta, newDelta, true, 1);
				logger.trace("Adding delta data (" + delta.getDelta() + ", 1): " + sql);
				rows = statement.executeUpdate(sql);
		
			}
		} catch (SQLException e) {
				e.printStackTrace();
			}
		return rows;
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, subSourceId, subSourceTable, superSourceId, superSourceTable)");
			sql.append("\n\t SELECT op1.class, op2.class, MIN(op1.id) AS subSourceId, '" + RelationName.onProperty + "' AS subSourceTable, MIN(op2.id) AS superSourceId, '" + RelationName.onProperty + "' AS superSourceTable");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT op1.class, op2.class ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("hasValue") + " AS hv1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op1 ON op1.class = hv1.class");
			sql.append("\n\t\t INNER JOIN hasValue AS hv2 ON hv1.value = hv2.value");
			sql.append("\n\t\t INNER JOIN onProperty AS op2 ON op2.class = hv2.class");
		} else if (run == 1) {
			sql.append("\n\t FROM hasValue AS hv1 ");
			sql.append("\n\t\t INNER JOIN onProperty AS op1 ON op1.class = hv1.class");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("hasValue") + " AS hv2 ON hv1.value = hv2.value");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op2 ON op2.class = hv2.class");
		}
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subProperty") + " AS sp ON sp.sub = op1.property AND sp.super = op2.property");

		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT sub, super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = op1.class AND bottom.super = op2.class) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY op1.class, op2.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1,C2) :- hasValue(C1, I), onProperty(C1, P1), hasValue(C2, I), onProperty(C2, P2), subProperty(P1, P2)";
	}

}
