package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class MembersRelation extends Relation {
	
	PreparedStatement addTypeStatement;
	
	protected MembersRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "members";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" colClass TEXT," +
					" list TEXT," +
					" PRIMARY KEY (colClass, list));" +
					" CREATE INDEX " + getTableName() + "_class ON " + getTableName() + "(colClass);" +
					" CREATE INDEX " + getTableName() + "_list ON " + getTableName() + "(list);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colClass, list) VALUES (?, ?)");

			addListStatement = conn.prepareStatement("INSERT INTO list (name, element) VALUES (?, ?)");
			addTypeStatement = conn.prepareStatement("INSERT INTO classAssertionEnt (entity, colClass) VALUES (?, ?)");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDifferentIndividualsAxiom) {
			OWLDifferentIndividualsAxiom naxiom = (OWLDifferentIndividualsAxiom) axiom;
			
			NodeID nid = nidMapper.get(naxiom);
			NodeID lid = NodeID.getNodeID();
			
			//members
			addStatement.setString(1, nid.toString());
			addStatement.setString(2, lid.toString());
			//addStatement.execute();
			
			//type
			addTypeStatement.setString(1, nid.toString());
			addTypeStatement.setString(2, OWLRDFVocabulary.OWL_ALL_DIFFERENT.getIRI().toString());
			addTypeStatement.execute();
			
			for (OWLIndividual ind : naxiom.getIndividuals()) {
				addListStatement.setString(1, lid.toString());
				if (ind.isAnonymous()) {
					addListStatement.setString(2, ind.asOWLAnonymousIndividual().getID().toString());
				} else {
					addListStatement.setString(2, ind.asOWLNamedIndividual().getIRI().toString());
				}
				
				addListStatement.execute();
			}
			
			return AdditionMode.ADD;
		} else if (axiom instanceof OWLDisjointClassesAxiom) {
			OWLDisjointClassesAxiom naxiom = (OWLDisjointClassesAxiom) axiom;
			
			NodeID nid = nidMapper.get(naxiom);
			NodeID lid = NodeID.getNodeID();
			
			//members
			addStatement.setString(1, nid.toString());
			addStatement.setString(2, lid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			//type
			addTypeStatement.setString(1, nid.toString());
			addTypeStatement.setString(2, OWLRDFVocabulary.OWL_ALL_DISJOINT_CLASSES.getIRI().toString());
			addTypeStatement.execute();
			
			for (OWLClassExpression ce : naxiom.getClassExpressions()) {
				addListStatement.setString(1, lid.toString());
				if (ce.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(ce).toString());
				} else {
					addListStatement.setString(2, ce.asOWLClass().getIRI().toString());
				}
				
				addListStatement.execute();
			}
			
			for (OWLClassExpression ce : naxiom.getClassExpressions()) {
				if (ce.isAnonymous()) {
					handleAnonymousClassExpression(ce);
				}
			}
			
			return AdditionMode.NOADD;
		} else if (axiom instanceof OWLDisjointDataPropertiesAxiom) {
			OWLDisjointDataPropertiesAxiom naxiom = (OWLDisjointDataPropertiesAxiom) axiom;
			
			NodeID nid = nidMapper.get(naxiom);
			NodeID lid = NodeID.getNodeID();
			
			//members
			addStatement.setString(1, nid.toString());
			addStatement.setString(2, lid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			//type
			addTypeStatement.setString(1, nid.toString());
			addTypeStatement.setString(2, OWLRDFVocabulary.OWL_ALL_DISJOINT_PROPERTIES.getIRI().toString());
			addTypeStatement.execute();
			
			for (OWLDataPropertyExpression dpe : naxiom.getProperties()) {
				addListStatement.setString(1, lid.toString());
				if (dpe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(dpe).toString());
				} else {
					addListStatement.setString(2, dpe.asOWLDataProperty().getIRI().toString());
				}
				
				addListStatement.execute();
			}
			
			for (OWLDataPropertyExpression dpe : naxiom.getProperties()) {
				if (dpe.isAnonymous()) {
					handleAnonymousDataPropertyExpression(dpe);
				}
			}
			
			return AdditionMode.NOADD;
		} else if (axiom instanceof OWLDisjointObjectPropertiesAxiom) {
			OWLDisjointObjectPropertiesAxiom naxiom = (OWLDisjointObjectPropertiesAxiom) axiom;
			
			NodeID nid = nidMapper.get(naxiom);
			NodeID lid = NodeID.getNodeID();
			
			//members
			addStatement.setString(1, nid.toString());
			addStatement.setString(2, lid.toString());
			addStatement.execute();
			reasonProcessor.add(new AdditionReason(this));
			
			//type
			addTypeStatement.setString(1, nid.toString());
			addTypeStatement.setString(2, OWLRDFVocabulary.OWL_ALL_DISJOINT_PROPERTIES.getIRI().toString());
			addTypeStatement.execute();
			
			for (OWLObjectPropertyExpression dpe : naxiom.getProperties()) {
				addListStatement.setString(1, lid.toString());
				if (dpe.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(dpe).toString());
				} else {
					addListStatement.setString(2, dpe.asOWLObjectProperty().getIRI().toString());
				}
				
				addListStatement.execute();
			}
			
			for (OWLObjectPropertyExpression dpe : naxiom.getProperties()) {
				if (dpe.isAnonymous()) {
					handleAnonymousObjectPropertyExpression(dpe);
				}
			}
			
			return AdditionMode.NOADD;
		}
		
		throw new U2R3NotImplementedException();

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
