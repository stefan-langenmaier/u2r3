package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsSvf1LitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsSvf1LitRule.class);
	
	ClsSvf1LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.someValuesFrom).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (literal, class, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4)");
			sql.append("\n\t SELECT prp.subject AS literal, svf.part AS class, ");
			sql.append(" MIN(svf.id) AS sourceId1, '" + RelationName.someValuesFrom + "' AS sourceTable1, ");
			sql.append(" MIN(op.id) AS sourceId2, '" + RelationName.onProperty + "' AS sourceTable2, ");
			sql.append(" MIN(prp.id) AS sourceId3, '" + RelationName.dataPropertyAssertion + "' AS sourceTable3, ");
			sql.append(" MIN(ca.id) AS sourceId4, '" + RelationName.classAssertionLit + "' AS sourceTable4 ");
		} else {
			sql.append(" (literal, class)");
			sql.append("\n\t SELECT DISTINCT prp.subject AS literal, svf.part AS class");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("someValuesFrom") + " AS svf");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON svf.part = op.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp ON prp.property = op.property");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionLit") + " AS ca ON ca.class = object AND ca.type = svf.total");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.literal");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.literal = prp.subject AND bottom.class = svf.part");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.subject, svf.part");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(U, X) :- someValuesFrom(X, Y), onProperty(X, P), dataPropertyAssertion(U, P, V), classAssertionLit(V, Y)";
	}

}
