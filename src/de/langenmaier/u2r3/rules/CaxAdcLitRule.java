package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class CaxAdcLitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(CaxAdcLitRule.class);
	
	CaxAdcLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.members).addAdditionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
	
		sql.append("SELECT '1' AS res");
		sql.append("\nFROM " + delta.getDeltaName("classAssertionEnt") + " AS clsA");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("members") + " AS m ON m.colClass = clsA.entity");
		sql.append("\n\t INNER JOIN list AS l ON m.list = l.name");
		
		sql.append("\n\t INNER JOIN classAssertionLit AS ca1 ON l.element = ca1.colClass");
		sql.append("\n\t INNER JOIN classAssertionLit AS ca2 ON l.element = ca2.colClass");

		sql.append("\n WHERE clsA.colClass = '" + OWLRDFVocabulary.OWL_ALL_DISJOINT_CLASSES + "'");
		sql.append("\n\t AND isSameLiteral(ca1.literal, ca2.literal, ca1.colClass, ca2.colClass, ca1.language, ca2.language) AND ca1.colClass != ca2.colClass ");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertion(X, all_disjoint_classes), members(X, Y), list(Y, C1..Cn), classAssertionEnt(Z, C1), classAssertionEnt(Z, C2)";
	}

}
