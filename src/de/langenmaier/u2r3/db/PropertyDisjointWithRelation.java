package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;

public class PropertyDisjointWithRelation extends Relation {
	static Logger logger = Logger.getLogger(PropertyDisjointWithRelation.class);
	
	protected PropertyDisjointWithRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "propertyDisjointWith";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, left VARCHAR(100), right VARCHAR(100), PRIMARY KEY (left, right))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDisjointObjectPropertiesAxiom) {
			OWLDisjointObjectPropertiesAxiom naxiom = (OWLDisjointObjectPropertiesAxiom) axiom;
			for (OWLObjectPropertyExpression pe1 : naxiom.getProperties()) {
				for (OWLObjectPropertyExpression pe2 : naxiom.getProperties()) {
					if (!pe1.equals(pe2)) {
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
					}
				}
				if (pe1.isAnonymous()) {
					handleAnonymousObjectPropertyExpression(pe1);
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
	
	public void merge(DeltaRelation delta) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
				return null;
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
	}


}
