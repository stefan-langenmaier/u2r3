package de.langenmaier.u2r3.rules;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmEqc2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmEqc2Rule.class);
	
	ScmEqc2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.equivalentClass;
		
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		
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
				rows += statement.executeUpdate(sql);
	
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
				rows += statement.executeUpdate(sql);
		
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
			sql.append(" (left, right, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT sc1.sub, sc2.sub,");
			sql.append(" MIN(sc1.id) AS sourceId1, '" + RelationName.subClass + "' AS sourceTable1,");
			sql.append(" MIN(sc2.id) AS sourceId2, '" + RelationName.subClass + "' AS sourceTable2");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT sc1.sub, sc2.sub ");
		}
		
		if (run == 0) {
			sql.append("\n\t FROM " + delta.getDeltaName("subClass") + " AS sc1 ");
			sql.append("\n\t\t INNER JOIN subClass AS sc2 ON sc1.super = sc2.sub AND sc1.sub = sc2.super");
		} else if (run == 1) {
			sql.append("\n\t FROM subClass AS sc1 ");
			sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("subClass") + " AS sc2 ON sc1.super = sc2.sub AND sc1.sub = sc2.super");
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = sc1.sub AND bottom.right = sc2.sub) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY sc1.sub, sc2.sub");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "equivalentClass(A,B) :- subClass(A,B), subClass(B,A)";
	}

}
