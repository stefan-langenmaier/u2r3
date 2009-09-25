package de.langenmaier.u2r3.db;

import java.net.URI;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

import de.langenmaier.u2r3.core.ReasonProcessor;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class DeclarationRelation extends Relation {
	
	protected DeclarationRelation() {
		try {
			tableName = "declaration";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE, subject VARCHAR(100), type VARCHAR(100), PRIMARY KEY (subject, type))");
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
		try {
			Statement stmt = conn.createStatement();
			long rows;
			
			//create compressed/compacted delta
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT subject, type FROM " + getTableName() + " AS bottom WHERE bottom.subject = t1.subject AND bottom.type = t1.type)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, subject, type) " +
					" SELECT id, subject, type " +
					" FROM " + getDeltaName(delta.getDelta()));

			
			
			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (Settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					//subjectSource
					sql = "SELECT id, '" + RelationName.declaration + "' AS table, subjectSourceId, subjectSourceTable FROM " + getDeltaName(delta.getDelta());
					RelationManager.addHistory(sql);
					
					//superSource
					sql = "SELECT id, '" + RelationName.declaration + "' AS table, typeSourceId, typeSourceTable FROM " + getDeltaName(delta.getDelta());
					RelationManager.addHistory(sql);
				}
				
				//fire reason
				logger.debug("Relation (" + toString()  + ") has got new data");
				Reason r = new AdditionReason(this, delta);
				ReasonProcessor.getReasonProcessor().add(r);
			}
			
			isDirty = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
