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
			sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3)");
			sql.append("\n\t SELECT prp.subject AS entity, svf.part AS colClass, ");
			sql.append(" MIN(svf.id) AS sourceId1, '" + RelationName.someValuesFrom + "' AS sourceTable1, ");
			sql.append(" MIN(prp.id) AS sourceId2, '" + RelationName.dataPropertyAssertion + "' AS sourceTable2, ");
			sql.append(" MIN(ca.id) AS sourceId3, '" + RelationName.classAssertionLit + "' AS sourceTable3 ");
		} else {
			sql.append(" (entity, colClass)");
			sql.append("\n\t SELECT DISTINCT prp.subject AS entity, svf.part AS colClass");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("someValuesFrom") + " AS svf");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp ON prp.property = svf.property");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionLit") + " AS ca ON ca.literal = object AND ca.colClass = svf.total");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.entity");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = prp.subject AND bottom.colClass = svf.part");
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
