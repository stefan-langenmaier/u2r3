package de.langenmaier.u2r3.util;

import org.semanticweb.owlapi.model.OWLDatatype;

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
}
