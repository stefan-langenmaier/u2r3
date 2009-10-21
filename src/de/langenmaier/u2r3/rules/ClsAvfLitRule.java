package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsAvfLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsAvfLitRule.class);
	
	ClsAvfLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionLit;
		
		relationManager.getRelation(RelationName.allValuesFrom).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, class, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4)");
			sql.append("\n\t SELECT prp.object AS entity, avf.total AS class, ");
			sql.append(" MIN(avf.id) AS sourceId1, '" + RelationName.allValuesFrom + "' AS sourceTable1, ");
			sql.append(" MIN(op.id) AS sourceId1, '" + RelationName.onProperty + "' AS sourceTable1, ");
			sql.append(" MIN(prp.id) AS sourceId1, '" + RelationName.dataPropertyAssertion + "' AS sourceTable1, ");
			sql.append(" MIN(ca.id) AS sourceId4, '" + RelationName.classAssertionEnt + "' AS sourceTable4 ");
		} else {
			sql.append(" (entity, class)");
			sql.append("\n\t SELECT DISTINCT prp.object AS class, svf.total AS type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("allValuesFrom") + " AS avf");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON avf.part = op.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("dataPropertyAssertion") + " AS prp ON prp.property = op.property");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca ON ca.class = prp.subject AND ca.type = avf.part");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.entity");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = prp.object AND bottom.class = avf.total");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.object, avf.total");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionLit(V, Y) :- allValuesFrom(X, Y), onProperty(X, P), dataPropertyAssertion(U, P, V), classAssertionEnt(U, X)";
	}

}
