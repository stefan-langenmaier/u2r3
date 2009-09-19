package de.langenmaier.u2r3.rules;

import java.sql.SQLException;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class TransSubClassRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	
	TransSubClassRule() {
		targetRelation = RelationName.subClass;
		
		RelationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		
		RelationManager.getRelation(RelationName.subClass).addDeletionRule(this);
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
		
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, subSourceId, superSourceId)");
		} else {
			sql.append(" (sub, super)");
		}
		
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t SELECT sub, super, MIN(subSourceId) AS subSourceId, MIN(superSourceId) AS superSourceId");
		} else {
			sql.append("\n\t SELECT DISTINCT sub, super ");
		}
				
		sql.append("\n\t FROM ( ");
		
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t\t SELECT t1.sub AS sub, t2.super AS super, t1.id AS subSourceId, t2.id AS superSourceId ");
		} else {
			sql.append("\n\t\t SELECT t1.sub AS sub, t2.super AS super ");
		}
		
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t\t FROM " + newDelta.getTableName() + " AS t1 INNER JOIN "+ newDelta.getTableName() + " AS t2 ");			
		} else {
			if (run == 1) {
				sql.append("\n\t\t FROM " + newDelta.getTableName() + " AS t1 INNER JOIN "+ delta.getDeltaName() + " AS t2 ");
			} else {
				sql.append("\n\t\t FROM " + delta.getDeltaName() + " AS t1 INNER JOIN "+ newDelta.getTableName() + " AS t2 ");
			}
		}
		
		sql.append(" WHERE t1.super = t2.sub  ");
		
		if (again) {
			if (Settings.getDeletionType() == DeletionType.CASCADING) {
				sql.append("\n\t\t\t AND NOT EXISTS (SELECT sub, super, subSourceId, superSourceId  FROM " + newDelta.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) ");
			} else {
				sql.append("\n\t\t\t AND NOT EXISTS (SELECT sub, super FROM " + newDelta.getDeltaName() + " AS bottom WHERE bottom.sub = t1.sub AND bottom.super = t2.super) ");
			}
		}
		
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t ) GROUP BY sub, super");
		} else {
			sql.append("\n\t )");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(A,C) :- subClass(A,B), subClass(B,C)";
	}

}
