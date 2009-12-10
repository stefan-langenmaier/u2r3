package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ImplNopaRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ImplSvfRule.class);
	
	ImplNopaRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		//relations on the right side
		relationManager.getRelation(RelationName.negativeObjectPropertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		String clazz = OWLRDFVocabulary.OWL_OBJECT_PROPERTY.getIRI().toString();
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, class, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT nopa.property, '" +  clazz + "',");
			sql.append(" MIN(nopa.id) AS sourceId1, '" + RelationName.negativeObjectPropertyAssertion + "' AS sourceTable1");
		} else {
			sql.append("(entity, class)");
			sql.append("\n\t SELECT DISTINCT nopa.property, '" +  clazz + "'");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("negativeObjectPropertyAssertion") + " AS nopa");


		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT entity, class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = nopa.property AND bottom.class = '" +  clazz + "'");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY nopa.property");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(X, ObjectProperty) :- negativeObjectPropertyAssertion(S, X, O)";
	}

}
