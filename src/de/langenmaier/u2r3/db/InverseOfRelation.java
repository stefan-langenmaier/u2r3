package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

public class InverseOfRelation extends Relation {
	static Logger logger = Logger.getLogger(InverseOfRelation.class);
	
	protected InverseOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "inverseOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" left TEXT," +
					" right TEXT," +
					" PRIMARY KEY (left, right));" +
					" CREATE HASH INDEX " + getTableName() + "_left ON " + getTableName() + "(left);" +
					" CREATE HASH INDEX " + getTableName() + "_right ON " + getTableName() + "(right);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (left, right) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
			OWLInverseObjectPropertiesAxiom naxiom = (OWLInverseObjectPropertiesAxiom) axiom;
			if (naxiom.getFirstProperty().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getFirstProperty()).toString());
				handleAnonymousObjectPropertyExpression(naxiom.getFirstProperty());
			} else {
				addStatement.setString(1, naxiom.getFirstProperty().asOWLObjectProperty().getIRI().toString());
			}
			if (naxiom.getSecondProperty().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getSecondProperty()).toString());
				handleAnonymousObjectPropertyExpression(naxiom.getSecondProperty());
			} else {
				addStatement.setString(2, naxiom.getSecondProperty().asOWLObjectProperty().getIRI().toString());
			}
			
			return AdditionMode.ADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}
	
	public void merge(DeltaRelation delta) {
		
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
