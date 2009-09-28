package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class CaxScoRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(CaxScoRule.class);
	
	CaxScoRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.declaration;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
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
		
			}
		} catch (SQLException e) {
				e.printStackTrace();
			}
		return rows;
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, type, subjectSourceId, subjectSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT dec.subject, sc.super, MIN(dec.id) AS subjectSourceId, '" + RelationName.declaration.toString() + "' AS subjectSourceTable, MIN(sc.id) AS typeSourceId, '" + RelationName.subClass.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (subject, type)");
			sql.append("\n\t SELECT DISTINCT dec.subject, sc.super");
		}
		
		//sql.append("\n\t FROM " + delta.getDeltaName() + " AS top");
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM declaration AS dec INNER JOIN subClass AS sc ON dec.type = sc.sub");
		} else {
			if (delta.getRelation() == relationManager.getRelation(RelationName.declaration)) {
				sql.append("\n\t FROM " + delta.getDeltaName() + " AS dec INNER JOIN subClass AS sc ON dec.type = sc.sub");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.subClass)) {
				sql.append("\n\t FROM declaration AS dec INNER JOIN " + delta.getDeltaName() + " AS sc ON dec.type = sc.sub");
			}
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = dec.subject AND bottom.type = sc.super");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY dec.subject, sc.super");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "declaration(X,C2) :- declaration(X, C1), subClass(C1, C2)";
	}

}
