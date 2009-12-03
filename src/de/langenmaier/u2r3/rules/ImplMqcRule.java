package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ImplMqcRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ImplSvfRule.class);
	
	ImplMqcRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		//relations on the right side
		relationManager.getRelation(RelationName.maxQualifiedCardinality).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		String clazz = OWLRDFVocabulary.OWL_RESTRICTION.getIRI().toString();
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, class, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT mqc.class, '" +  clazz + "',");
			sql.append(" MIN(mqc.id) AS sourceId1, '" + RelationName.maxQualifiedCardinality + "' AS sourceTable1");
		} else {
			sql.append("(entity, class)");
			sql.append("\n\t SELECT DISTINCT mqc.class, '" +  clazz + "',");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("maxQualifiedCardinality") + " AS mc");


		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT entity, class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = mqc.class AND bottom.class = '" +  clazz + "'");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY mqc.class");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(X, Restriction) :- maxQualifiedCardinality(X, P, T, C)";
	}

}
