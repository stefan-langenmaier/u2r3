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

		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_BYTE.getIRI().toString() + "', '" + OWL2Datatype.XSD_UNSIGNED_SHORT.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_SHORT.getIRI().toString() + "', '" + OWL2Datatype.XSD_UNSIGNED_INT.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_INT.getIRI().toString() + "', '" + OWL2Datatype.XSD_UNSIGNED_LONG.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_LONG.getIRI().toString() + "', '" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_POSITIVE_INTEGER.getIRI().toString() + "', '" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.getIRI().toString() + "', '" + OWL2Datatype.XSD_INTEGER.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NEGATIVE_INTEGER.getIRI().toString() + "', '" + OWL2Datatype.XSD_NON_POSITIVE_INTEGER.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NON_POSITIVE_INTEGER.getIRI().toString() + "', '" + OWL2Datatype.XSD_INTEGER.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_INTEGER.getIRI().toString() + "', '" + OWL2Datatype.XSD_DECIMAL.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NORMALIZED_STRING.getIRI().toString() + "', '" + OWL2Datatype.XSD_STRING.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_TOKEN.getIRI().toString() + "', '" + OWL2Datatype.XSD_NORMALIZED_STRING.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_LANGUAGE.getIRI().toString() + "', '" + OWL2Datatype.XSD_TOKEN.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NAME.getIRI().toString() + "', '" + OWL2Datatype.XSD_TOKEN.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NCNAME.getIRI().toString() + "', '" + OWL2Datatype.XSD_NAME.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NMTOKEN.getIRI().toString() + "', '" + OWL2Datatype.XSD_TOKEN.getIRI().toString() + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_DATE_TIME_STAMP.getIRI().toString() + "', '" + OWL2Datatype.XSD_DATE_TIME.getIRI().toString() + "')");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(subDatatype, superDatatype) :- true";
	}

}
