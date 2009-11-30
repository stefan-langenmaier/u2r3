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
		if (datatype.isBuiltIn()) {
			try {
				if (datatype.isBoolean()) {
					return String.valueOf(Boolean.parseBoolean(literal));
				}
				if (datatype.isBoolean()) {
					return String.valueOf(Integer.parseInt(literal));
				}
				if (datatype.isFloat()) {
					return String.valueOf(Float.parseFloat(literal));
				}
				if (datatype.isDouble()) {
					return String.valueOf(Double.parseDouble(literal));
				}
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		return literal;
	}

	public static boolean isValid(String literal, OWLDatatype dt) {
		if (dt.isBuiltIn()) {
			try {
				if (dt.isBoolean()) {
					Boolean.parseBoolean(literal);
				}
				if (dt.isBoolean()) {
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
		
		if (lt1.equals(lt2) && dt1.equals(dt2) && l1.equals(l2)) {
			return true;
		}
		return false;
	}
}
