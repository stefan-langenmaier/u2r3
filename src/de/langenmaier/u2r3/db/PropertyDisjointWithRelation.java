package de.langenmaier.u2r3.db;

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
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" left TEXT," +
					" right TEXT," +
					" PRIMARY KEY (left, right));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(left);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(right);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
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
					handleAnonymousObjectPropertyExpression(pe1);
				}
				
				if (pe2.isAnonymous()) {
					handleAnonymousObjectPropertyExpression(pe2);
				}
				
				return AdditionMode.NOADD;
			}
			
		} else if (axiom instanceof OWLDisjointDataPropertiesAxiom) {
			OWLDisjointDataPropertiesAxiom naxiom = (OWLDisjointDataPropertiesAxiom) axiom;
			if (naxiom.getProperties().size() == 2) {
				Iterator<OWLDataPropertyExpression> it = naxiom.getProperties().iterator();
				OWLDataPropertyExpression pe1 = it.next();
				OWLDataPropertyExpression pe2 = it.next();
				
				if (pe1.isAnonymous()) {
					addStatement.setString(1, nidMapper.get(pe1).toString());
				} else {
					addStatement.setString(1, pe1.asOWLDataProperty().getIRI().toString());
				}
				if (pe2.isAnonymous()) {
					addStatement.setString(2, nidMapper.get(pe2).toString());
				} else {
					addStatement.setString(2, pe2.asOWLDataProperty().getIRI().toString());
				}
				
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (pe1.isAnonymous()) {
					handleAnonymousDataPropertyExpression(pe1);
				}
				
				if (pe2.isAnonymous()) {
					handleAnonymousDataPropertyExpression(pe2);
				}
				
				return AdditionMode.NOADD;
			}
		}
		
		throw new U2R3NotImplementedException();
		
	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}
	
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
