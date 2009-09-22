package de.langenmaier.u2r3.db;

import java.net.URI;
import java.sql.SQLException;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Pair;

public class DeclarationRelation extends Relation {
	
	protected DeclarationRelation() {
		try {
			tableName = "declaration";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, subject VARCHAR(100), type VARCHAR(100), PRIMARY KEY (subject))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, type) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addImpl(OWLAxiom axiom) throws SQLException {
		OWLDeclarationAxiom naxiom = (OWLDeclarationAxiom) axiom;
		addStatement.setString(1, naxiom.getEntity().getIRI().toString());
		
		OWLEntity entity = naxiom.getEntity();
		URI type = null;
		if (entity.isOWLAnnotationProperty()) {
			type = OWLXMLVocabulary.ANNOTATION_PROPERTY.getURI();
		}
		if (entity.isOWLClass()) {
			type = OWLXMLVocabulary.CLASS.getURI();
		}
		if (entity.isOWLDataProperty()) {
			type = OWLXMLVocabulary.DATA_PROPERTY.getURI();
		}
		if (entity.isOWLDatatype()) {
			type = OWLXMLVocabulary.DATATYPE.getURI();
		}
		if (entity.isOWLNamedIndividual()) {
			type = OWLXMLVocabulary.NAMED_INDIVIDUAL.getURI();
		}
		if (entity.isOWLObjectProperty()) {
			type = OWLXMLVocabulary.OBJECT_PROPERTY.getURI();
		}
		addStatement.setString(2, type.toString());
		System.out.println(type.toString());
	}

	@Override
	public void createDeltaImpl(long id) {
		try {
			dropDelta(id);
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "" +
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject VARCHAR(100), type VARCHAR(100)," +
					" subjectSourceId UUID," +
					" subjectSourceTable VARCHAR(100)," +
					" typeSourceId UUID," +
					" typeSourceTable VARCHAR(100)," +
					" PRIMARY KEY (subject, type))");
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
