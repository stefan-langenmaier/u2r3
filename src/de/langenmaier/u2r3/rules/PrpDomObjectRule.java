package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpDomObjectRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpDomObjectRule.class);
	
	PrpDomObjectRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.declaration;
		
		//relations on the right side
		relationManager.getRelation(RelationName.objectPropertyDomain).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
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
			sql.append("\n\t SELECT ass.subject, dom.Domain, MIN(ass.id) AS subjectSourceId, '" + RelationName.objectPropertyAssertion + "' AS subjectSourceTable, MIN(dom.id) AS typeSourceId, '" + RelationName.objectPropertyDomain + "' AS typeSourceTable");
		} else {
			sql.append("(subject, type)");
			sql.append("\n\t SELECT DISTINCT ass.subject, dom.Domain");
		}
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM objectPropertyAssertion AS ass");
			sql.append("\n\t\t INNER JOIN objectPropertyDomain AS dom");
		} else {
			if (relationManager.getRelation(RelationName.objectPropertyAssertion) == delta.getRelation()) {
				sql.append("\n\t FROM " + delta.getDeltaName() + " AS ass");
				sql.append("\n\t\t INNER JOIN objectPropertyDomain AS dom");
			} else {
				sql.append("\n\t FROM objectPropertyAssertion AS ass");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS dom");
			}
		}
		sql.append("\n\t\t ON ass.Property = dom.Property");

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT subject, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = ass.subject AND bottom.type = dom.domain");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ass.subject, dom.Domain");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "declaration(X, C) :- objectPropertyDomain(P, C), objectPropertyAssertion(X, P, Y)";
	}

}
