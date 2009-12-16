package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class EqDiff2Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(EqDiff2Rule.class);
	
	EqDiff2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.sameAsEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.members).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		sql.append("\n FROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("members") + " AS m ON clsA.entity = m.class");
		sql.append("\n\t INNER JOIN list AS l1 ON m.list = l1.name");
		sql.append("\n\t INNER JOIN list AS l2 ON m.list = l2.name");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("sameAsEnt") + " AS sa ON l1.element = sa.left AND l2.element = sa.right AND l1.name = l2.name AND sa.left != sa.right");
		sql.append("\n WHERE clsA.class = '" + OWLRDFVocabulary.OWL_ALL_DIFFERENT.getIRI().toString() + "'");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertionEnt(X, alldifferent), members(X, Y), list(Y, Z1..Zn), sameAsEnt(Zi, Zj)";
	}
 
}
