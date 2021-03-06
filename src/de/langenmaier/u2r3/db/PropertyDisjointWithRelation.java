package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class PropertyDisjointWithRelation extends Relation {
	static Logger logger = Logger.getLogger(PropertyDisjointWithRelation.class);
	
	protected PropertyDisjointWithRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyDisjointWith";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colLeft TEXT," +
		" colRight TEXT," +
		" PRIMARY KEY (colLeft, colRight));" +
		" CREATE INDEX " + table + "_left ON " + table + "(colLeft);" +
		" CREATE INDEX " + table + "_right ON " + table + "(colRight);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colLeft, colRight) VALUES (?, ?)";
	}


	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDisjointObjectPropertiesAxiom) {
			OWLDisjointObjectPropertiesAxiom naxiom = (OWLDisjointObjectPropertiesAxiom) axiom;
			if (naxiom.getProperties().size() == 2) {
				Iterator<OWLObjectPropertyExpression> it = naxiom.getProperties().iterator();
				OWLObjectPropertyExpression pe1 = it.next();
				OWLObjectPropertyExpression pe2 = it.next();
				
				if (pe1.isAnonymous()) {
					addStatement.setString(1, nidMapper.get(pe1).toString());
				} else {
					addStatement.setString(1, pe1.asOWLObjectProperty().getIRI().toString());
				}
				if (pe2.isAnonymous()) {
					addStatement.setString(2, nidMapper.get(pe2).toString());
				} else {
					addStatement.setString(2, pe2.asOWLObjectProperty().getIRI().toString());
				}
				
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (pe1.isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(pe1);
				}
				
				if (pe2.isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(pe2);
				}
				
				return AdditionMode.NOADD;
			}
			
		} else if (axiom instanceof OWLDisjointDataPropertiesAxiom) {
			OWLDisjointDataPropertiesAxiom naxiom = (OWLDisjointDataPropertiesAxiom) axiom;
			if (naxiom.getProperties().size() == 2) {
				Iterator<OWLDataPropertyExpression> it = naxiom.getProperties().iterator();
				OWLDataPropertyExpression pe1 = it.next();
				OWLDataPropertyExpression pe2 = it.next();
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
					if (pe1.isAnonymous()) {
						add.setString(1, nidMapper.get(pe1).toString());
					} else {
						add.setString(1, pe1.asOWLDataProperty().getIRI().toString());
					}
					if (pe2.isAnonymous()) {
						add.setString(2, nidMapper.get(pe2).toString());
					} else {
						add.setString(2, pe2.asOWLDataProperty().getIRI().toString());
					}
					
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
				if (pe1.isAnonymous()) {
					handleAddAnonymousDataPropertyExpression(pe1);
				}
				
				if (pe2.isAnonymous()) {
					handleAddAnonymousDataPropertyExpression(pe2);
				}
				
				return AdditionMode.NOADD;
			}
		}
		
		throw new U2R3NotImplementedException();
		
	}

}
