package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsAvfEntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsAvfEntRule.class);
	
	ClsAvfEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.allValuesFrom).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3)");
			sql.append("\n\t SELECT prp.object AS entity, avf.total AS colClass, ");
			sql.append(" MIN(avf.id) AS sourceId1, '" + RelationName.allValuesFrom + "' AS sourceTable1, ");
			sql.append(" MIN(prp.id) AS sourceId2, '" + RelationName.objectPropertyAssertion + "' AS sourceTable2, ");
			sql.append(" MIN(ca.id) AS sourceId3, '" + RelationName.classAssertionEnt + "' AS sourceTable3 ");
		} else {
			sql.append(" (entity, colClass)");
			sql.append("\n\t SELECT DISTINCT prp.object AS colClass, avf.total AS type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("allValuesFrom") + " AS avf");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("objectPropertyAssertion") + " AS prp ON prp.property = avf.property");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca ON ca.entity = prp.subject AND ca.colClass = avf.part");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.entity");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = prp.object AND bottom.colClass = avf.total");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.object, avf.total");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(V, Y) :- allValuesFrom(X, Y), onProperty(X, P), objectPropertyAssertion(U, P, V), classAssertionEnt(U, X)";
	}

}
