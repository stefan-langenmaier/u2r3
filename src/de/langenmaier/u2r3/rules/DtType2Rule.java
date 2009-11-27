package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;

public class DtType2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(DtType2Rule.class);
	
	DtType2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		sql.append(" (sub, super) VALUES ");

		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_BYTE.toString() + "', '" + OWL2Datatype.XSD_UNSIGNED_SHORT.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_SHORT.toString() + "', '" + OWL2Datatype.XSD_UNSIGNED_INT.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_INT.toString() + "', '" + OWL2Datatype.XSD_UNSIGNED_LONG.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_LONG.toString() + "', '" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_POSITIVE_INTEGER.toString() + "', '" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.toString() + "', '" + OWL2Datatype.XSD_INTEGER.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NEGATIVE_INTEGER.toString() + "', '" + OWL2Datatype.XSD_NON_POSITIVE_INTEGER.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NON_POSITIVE_INTEGER.toString() + "', '" + OWL2Datatype.XSD_INTEGER.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_INTEGER.toString() + "', '" + OWL2Datatype.XSD_DECIMAL.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NORMALIZED_STRING.toString() + "', '" + OWL2Datatype.XSD_STRING.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_TOKEN.toString() + "', '" + OWL2Datatype.XSD_NORMALIZED_STRING.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_LANGUAGE.toString() + "', '" + OWL2Datatype.XSD_TOKEN.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NAME.toString() + "', '" + OWL2Datatype.XSD_TOKEN.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NCNAME.toString() + "', '" + OWL2Datatype.XSD_NAME.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NMTOKEN.toString() + "', '" + OWL2Datatype.XSD_TOKEN.toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_DATE_TIME_STAMP.toString() + "', '" + OWL2Datatype.XSD_DATE_TIME.toString() + "')");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(subDatatype, superDatatype) :- true";
	}

}
