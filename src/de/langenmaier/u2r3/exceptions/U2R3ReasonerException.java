package de.langenmaier.u2r3.exceptions;

import org.semanticweb.owlapi.reasoner.OWLReasonerException;

public class U2R3ReasonerException extends OWLReasonerException {

	public U2R3ReasonerException(Throwable cause) {
		super(cause);
	}
	
	public U2R3ReasonerException(String msg) {
		super(msg);
	}
	
	public U2R3ReasonerException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2863133915001944746L;

}
