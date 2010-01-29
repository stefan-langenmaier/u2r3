package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public interface Query {
	
	/**
	 * Liefert ein Statement zurück mit dem man in der Datenbank
	 * nach dem Axiom suchen kann. Es soll dabei die Tabelle und
	 * die uid zurueckgegeben werden.
	 * @param ax
	 * @return
	 * @throws SQLException 
	 */
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException;
	
	void getSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col);
	
	void getSubAxiomLocationImpl(StringBuilder sql, OWLObjectPropertyExpression pe, String tid, String col);
	
	void getSubAxiomLocationImpl(StringBuilder sql, OWLDataPropertyExpression pe, String tid, String col);
	
	void getSubAxiomLocationImpl(StringBuilder sql, OWLIndividual ind, String tid, String col);
	
	void handleSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col);
	
	void handleSubAxiomLocationImpl(StringBuilder sql, OWLObjectPropertyExpression pe, String tid, String col);
	
	void handleSubAxiomLocationImpl(StringBuilder sql, OWLDataPropertyExpression pe, String tid, String col);
	
	void handleSubAxiomLocationImpl(StringBuilder sql, OWLIndividual ind, String tid, String col);

}
