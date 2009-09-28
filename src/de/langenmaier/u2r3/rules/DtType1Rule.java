package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;

public class DtType1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(DtType1Rule.class);
	
	DtType1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.declaration;
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		String datatype = OWLRDFVocabulary.OWL_DATATYPE.getURI().toString();
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		sql.append(" (subject, type) VALUES ");

		sql.append("\n ('" + OWLRDFVocabulary.RDF_PLAIN_LITERAL.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.RDF_XML_LITERAL.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWLRDFVocabulary.RDFS_LITERAL.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_DECIMAL.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_INTEGER.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NON_POSITIVE_INTEGER.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_POSITIVE_INTEGER.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NEGATIVE_INTEGER.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_LONG.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_INT.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_SHORT.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_BYTE.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_LONG.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_INT.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_SHORT.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_UNSIGNED_BYTE.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_FLOAT.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_DOUBLE.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_STRING.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NORMALIZED_STRING.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_TOKEN.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_LANGUAGE.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NAME.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NCNAME.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_NMTOKEN.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_BOOLEAN.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_HEX_BINARY.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_BASE_64_BINARY.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_ANY_URI.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_DATE_TIME.getURI().toString() + "', '" + datatype + "'),");
		sql.append("\n ('" + OWL2Datatype.XSD_DATE_TIME_STAMP.getURI().toString() + "', '" + datatype + "')");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "declaration(dt, datatype) :- true";
	}

}
