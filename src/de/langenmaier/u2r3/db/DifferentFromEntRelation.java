package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.TableId;

public class DifferentFromEntRelation extends Relation {
	
	protected DifferentFromEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "differentFromEnt";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colLeft TEXT," +
		" colRight TEXT," +
		" PRIMARY KEY (id));" +
		" CREATE INDEX " + table + "_left ON " + table + "(colLeft);" +
		" CREATE INDEX " + table + "_right ON " + table + "(colRight);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colLeft, colRight) VALUES (?, ?)";
	}

	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDifferentIndividualsAxiom) {
			OWLDifferentIndividualsAxiom naxiom = (OWLDifferentIndividualsAxiom) axiom;
			//if only two individuals are specified this table can be use
			if (naxiom.getIndividuals().size() == 2) {
				OWLIndividual ind1 = naxiom.getIndividualsAsList().get(0);
				OWLIndividual ind2 = naxiom.getIndividualsAsList().get(1);
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
					if (ind1.isAnonymous()) {
						add.setString(1, ind1.asOWLAnonymousIndividual().getID().toString());
					} else {
						add.setString(1, ind1.asOWLNamedIndividual().getIRI().toString());
					}
					if (ind2.isAnonymous()) {
						add.setString(2, ind2.asOWLAnonymousIndividual().getID().toString());
					} else {
						add.setString(2, ind2.asOWLNamedIndividual().getIRI().toString());
					}
				}
			} else {//otherwise it should in the list table
				//_:x rdf:type owl:AllDifferent.
				//_:x owl:members (a1 … an). 
				throw new U2R3RuntimeException();
			}


			return AdditionMode.ADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}
	
	@Override
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		if (ax instanceof OWLDifferentIndividualsAxiom) {
			OWLDifferentIndividualsAxiom nax = (OWLDifferentIndividualsAxiom) ax;
			String left = null;
			String right = null;
			String tableId = TableId.getId();
			
			if (nax.getIndividuals().size() == 2) {
				if (!nax.getIndividualsAsList().get(0).isAnonymous()) {
					left = nax.getIndividualsAsList().get(0).asOWLNamedIndividual().getIRI().toString();
				}
				
				if (!nax.getIndividualsAsList().get(1).isAnonymous()) {
					right = nax.getIndividualsAsList().get(1).asOWLNamedIndividual().getIRI().toString();
				}		
				
				
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
				sql.append("\nFROM  " + getTableName() + " AS " + tableId);
				sql.append("\nWHERE ");
				if (left != null) {
					sql.append("colLeft='" + left + "' ");
				} else {
					sql.append(" EXISTS ");
					handleSubAxiomLocationImpl(sql, nax.getIndividualsAsList().get(0), tableId, "colLeft");
				}
				
				if (right != null) {
					sql.append(" AND colRight='" + right + "'");
				} else {
					sql.append(" AND EXISTS ");
					handleSubAxiomLocationImpl(sql, nax.getIndividualsAsList().get(1), tableId, "colRight");
				}
				PreparedStatement stmt = conn.prepareStatement(sql.toString());
				return stmt;
			} else {
				return relationManager.getRelation(RelationName.members).getAxiomLocation(ax);
			}
		}
		throw new U2R3RuntimeException();
	}

}
