package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsSvf2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsSvf2Rule.class);
	
	ClsSvf2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.someValuesFrom).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT prp.subject AS class, svf.part AS type, MIN(prp.id) AS classSourceId, '" + RelationName.propertyAssertion + "' AS classSourceTable, MIN(svf.id) AS typeSourceId, '" + RelationName.someValuesFrom + "' AS typeSourceTable ");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT prp.subject AS class, svf.part AS type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("someValuesFrom") + " AS svf");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON svf.part = op.class AND svf.total = '" + OWLRDFVocabulary.OWL_THING + "'");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp ON prp.property = op.property");

		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = prp.subject AND bottom.type = svf.part");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.subject, svf.part");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(U, X) :- someValuesFrom(X, thing), onProperty(X, P), propertyAssertion(U, P, V)";
	}

}
