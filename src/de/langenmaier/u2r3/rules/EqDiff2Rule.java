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
		
		relationManager.getRelation(RelationName.sameAs).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.members).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		sql.append("\n FROM " + delta.getDeltaName("classAssertion") + " AS clsA");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("members") + " AS m ON clsA.class = m.class");
		sql.append("\n\t INNER JOIN list AS l ON m.list = l.name");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("sameAs") + " AS sa ON l.element = sa.left AND sa.left != sa.right");
		sql.append("\n WHERE clsA.type = '" + OWLRDFVocabulary.OWL_ALL_DIFFERENT.getIRI().toString() + "'");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertion(X, alldifferent), members(X, Y), list(Y, Z1..Zn), sameAs(Zi, Zj)";
	}

}