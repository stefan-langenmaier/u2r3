package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class DtrSvfRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(DtrSvfRule.class);
	
	DtrSvfRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.someValuesFrom).addAdditionRule(this);
		relationManager.getRelation(RelationName.datatypeRestriction).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable4)");
			sql.append("\n\t SELECT dpa.subject, svf.part, ");
			sql.append(" MIN(dpa.id) AS sourceId1, '" + RelationName.dataPropertyAssertion + "' AS sourceTable1, ");
			sql.append(" MIN(svf.id) AS sourceId2, '" + RelationName.someValuesFrom + "' AS sourceTable2, ");
			sql.append(" MIN(dtr.id) AS sourceId3, '" + RelationName.datatypeRestriction + "' AS sourceTable3");
		} else {
			sql.append(" (entity, colClass)");
			sql.append("\n\t SELECT DISTINCT dpa.subject, svf.part");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("dataPropertyAssertion") + " AS dpa");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("someValuesFrom") + " AS svf ON dpa.property = svf.property");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("datatypeRestriction") + " AS dtr ON dtr.colClass = svf.total");
		sql.append("\n\t WHERE isInFacet(dtr.list, dpa.object, dpa.type)"); 
		
		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT 1");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = dpa.subject AND bottom.colClass = svf.part");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY dpa.subject, svf.part");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(S, X) :- dataPropertyAssertion(S, P, V), someValuesFrom(X, P, Y), datatypeRestriction(Y, R), V in R";
	}

}
