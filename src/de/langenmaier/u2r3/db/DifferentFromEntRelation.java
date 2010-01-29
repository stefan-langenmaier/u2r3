package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class DifferentFromEntRelation extends Relation {
	
	protected DifferentFromEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "differentFromEnt";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" PRIMARY KEY (id));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(colLeft);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(colRight);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDifferentIndividualsAxiom) {
			OWLDifferentIndividualsAxiom naxiom = (OWLDifferentIndividualsAxiom) axiom;
			//if only two individuals are specified this table can be use
			if (naxiom.getIndividuals().size() == 2) {//TODO Code vereinfachen
				for (OWLIndividual ind1 : naxiom.getIndividuals()) {
					for (OWLIndividual ind2 : naxiom.getIndividuals()) {
						if (!ind1.equals(ind2)) {
							if (ind1.isAnonymous()) {
								addStatement.setString(1, ind1.asOWLAnonymousIndividual().getID().toString());
							} else {
								addStatement.setString(1, ind1.asOWLNamedIndividual().getIRI().toString());
							}
							if (ind2.isAnonymous()) {
								addStatement.setString(2, ind2.asOWLAnonymousIndividual().getID().toString());
							} else {
								addStatement.setString(2, ind2.asOWLNamedIndividual().getIRI().toString());
							}
							
							addStatement.execute();
							reasonProcessor.add(new AdditionReason(this));
						}
					}
				}
			} else {//otherwise it should in the list table
				//_:x rdf:type owl:AllDifferent.
				//_:x owl:members (a1 … an). 
				for (OWLIndividual ind1 : naxiom.getIndividuals()) {
					for (OWLIndividual ind2 : naxiom.getIndividuals()) {
						if (!ind1.equals(ind2)) {
							if (ind1.isAnonymous()) {
								addStatement.setString(1, ind1.asOWLAnonymousIndividual().getID().toString());
							} else {
								addStatement.setString(1, ind1.asOWLNamedIndividual().getIRI().toString());
							}
							if (ind2.isAnonymous()) {
								addStatement.setString(2, ind2.asOWLAnonymousIndividual().getID().toString());
							} else {
								addStatement.setString(2, ind2.asOWLNamedIndividual().getIRI().toString());
							}
							
							addStatement.execute();
							reasonProcessor.add(new AdditionReason(this));
						}
					}
				}
			}


			return AdditionMode.NOADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void merge(DeltaRelation delta) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}

}
