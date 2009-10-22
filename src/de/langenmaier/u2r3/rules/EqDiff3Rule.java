package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class EqDiff3Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(EqDiff3Rule.class);
	
	EqDiff3Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.sameAsEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.distinctMembers).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		sql.append("\n FROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("distinctMembers") + " AS m ON clsA.entity = m.class");
		sql.append("\n\t INNER JOIN list AS l ON m.list = l.name");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("sameAsEnt") + " AS sa ON l.element = sa.left AND sa.left != sa.right");
		sql.append("\n WHERE clsA.class = '" + OWLRDFVocabulary.OWL_ALL_DIFFERENT.getIRI().toString() + "'");

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertionEnt(X, alldifferent), distinctMembers(X, Y), list(Y, Z1..Zn), sameAsEnt(Zi, Zj)";
	}

}
