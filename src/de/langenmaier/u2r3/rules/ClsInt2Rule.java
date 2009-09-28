package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsInt2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsInt2Rule.class);
	
	ClsInt2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.declaration;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
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
			sql.append("\n\t SELECT dec.subject, l.element, MIN(dec.id) AS subjectSourceId, '" + RelationName.declaration.toString() + "' AS subjectSourceTable, MIN(l.id) AS typeSourceId, '" + RelationName.list.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (subject, type)");
			sql.append("\n\t SELECT DISTINCT dec.subject, l.element");
		}
		
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM  intersectionOf AS int INNER JOIN list AS l");
			sql.append("\n\t\t ON int.list = l.name");
			sql.append("\n\t\t INNER JOIN declaration AS dec");
			sql.append("\n\t\t ON dec.type = int.class");
		} else {
			if (delta.getRelation() == relationManager.getRelation(RelationName.intersectionOf)) {
				sql.append("\n\t FROM  " + delta.getDeltaName() + " AS int INNER JOIN list AS l");
				sql.append("\n\t\t ON int.list = l.name");
				sql.append("\n\t\t INNER JOIN declaration AS dec");
				sql.append("\n\t\t ON dec.type = int.class");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.list)) {
				sql.append("\n\t FROM  intersectionOf AS int INNER JOIN " + delta.getDeltaName() + " AS l");
				sql.append("\n\t\t ON int.list = l.name");
				sql.append("\n\t\t INNER JOIN declaration AS dec");
				sql.append("\n\t\t ON dec.type = int.class");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.declaration)) {
				sql.append("\n\t FROM  intersectionOf AS int INNER JOIN list AS l");
				sql.append("\n\t\t ON int.list = l.name");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS dec");
				sql.append("\n\t\t ON dec.type = int.class");
			}
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = dec.subject AND bottom.type = l.element");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY dec.subject, l.element");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "declaration(Y,C1..Cn) :- intersectionOf(C, X), list(x, C1..Cn), declaration(Y, C)";
	}

}
