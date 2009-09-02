package de.langenmaier.u2r3.exceptions;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Gets thrown when an OWL2 Class should be process that is not in the RL Profile
 * @author stefan
 *
 */
public class U2R3NotInProfileException extends U2R3RuntimeException {

	/**
	 * This axiom is not in the OWL2 RL Profile
	 * @param axiom
	 */
	public U2R3NotInProfileException(OWLAxiom axiom) {
		//TODO Wo gibt man an was das Axiom war?
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4795248548692948171L;

}
