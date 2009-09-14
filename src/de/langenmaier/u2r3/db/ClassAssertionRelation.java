package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Pair;

public class ClassAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(ClassAssertionRelation.class);
	
	protected ClassAssertionRelation() {
		try {
			tableName = "classAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (class, type) VALUES (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
			OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) axiom;
			addStatement.setString(1, naxiom.getIndividual().asNamedIndividual().getURI().toString());
			addStatement.setString(2, naxiom.getClassExpression().asOWLClass().getIRI().toURI().toString());
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + " (class VARCHAR(100), type VARCHAR(100), PRIMARY KEY (class, type))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void merge(DeltaRelation delta) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
