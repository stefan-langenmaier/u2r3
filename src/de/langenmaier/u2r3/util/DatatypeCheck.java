package de.langenmaier.u2r3.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;

public class DatatypeCheck {
	/**
	 * Checks if the builtin datatypes are correct and brings them to unified format
	 * @param literal
	 * @param datatype
	 * @return a unified format of the literal
	 */
	public static String validateType(String literal, OWLDatatype datatype) {
		if (isValid(literal, datatype)) {
			return literal;
		}
		return null;
	}

	public static boolean isValid(String literal, OWLDatatype dt) {
		if (dt.isBuiltIn()) {
			try {
				if (dt.isBoolean()) {
					Boolean.parseBoolean(literal);
				}
				if (dt.isInteger()) {
					Integer.parseInt(literal);
				}
				if (dt.isFloat()) {
					Float.parseFloat(literal);
				}
				if (dt.isDouble()) {
					Double.parseDouble(literal);
				}
				return true;
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isSameLiteral(String lt1, String lt2, String dt1, String dt2, String l1, String l2) {
		if (l1 == null) {
			l1 = "";
		}
		if (l2 == null) {
			l2 = "";
		}
		
		if (dt1 == null) {
			dt1 = "";
		}
		if (dt2 == null) {
			dt2 = "";
		}
		
		if (lt1.equals(lt2) && dt1.equals(dt2) && l1.equals(l2)) {
			return true;
		}
		return false;
	}
	
	public static boolean isInFacet(java.sql.Connection conn, String facetList, String literal, String type) throws SQLException {
		IRI dt = IRI.create(type);
		if (OWL2Datatype.isBuiltIn(dt)) {
			if (OWL2Datatype.getDatatype(dt).getCategory() == OWL2Datatype.Category.NUMBER) {
				Statement restrictionsQuery = conn.createStatement();
				String sql = "SELECT facet, value, type, language" + 
				" FROM facetList AS fl " +
				" WHERE fl.name = '" + facetList + "'";
				ResultSet restrictions = restrictionsQuery.executeQuery(sql);
				double value = Double.parseDouble(literal);
				while(restrictions.next()) {
					// V in R for every facet
					String facet = restrictions.getString("facet");
					double comparision = Double.parseDouble(restrictions.getString("value"));
					
					//String type = restrictions.getString("type");
					if (facet.equals(OWLFacet.MIN_INCLUSIVE.getIRI().toString())) {
						if (!(value >= comparision)) {
							return false;
						}
					} else if (facet.equals(OWLFacet.MIN_EXCLUSIVE.getIRI().toString())) {
						if (!(value > comparision)) {
							return false;
						}
					} else if (facet.equals(OWLFacet.MAX_EXCLUSIVE.getIRI().toString())) {
						if (!(value < comparision)) {
							return false;
						}
					} else if (facet.equals(OWLFacet.MAX_INCLUSIVE.getIRI().toString())) {
						if (!(value <= comparision)) {
							return false;
						}
					} else {
						return false;
					}
				}
				return true;
			}
		}
		return false;
		
	}
}
