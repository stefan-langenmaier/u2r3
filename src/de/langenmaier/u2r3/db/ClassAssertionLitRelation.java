package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLStringLiteral;
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.DatatypeCheck;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClassAssertionLitRelation extends Relation {
	static Logger logger = Logger.getLogger(ClassAssertionLitRelation.class);
	
	protected ClassAssertionLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "classAssertionLit";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" literal TEXT," +
					" colClass TEXT," +
					" language TEXT," +
					" PRIMARY KEY (id, literal, colClass));" +
					" CREATE INDEX " + getTableName() + "_literal ON " + getTableName() + "(literal);" +
					" CREATE INDEX " + getTableName() + "_class ON " + getTableName() + "(colClass);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (literal, colClass, language) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();
	}
	
	@Override
	public void add(OWLObject o) {
		try {
			if (o instanceof OWLTypedLiteral) {
				OWLTypedLiteral tl = (OWLTypedLiteral) o;
				addStatement.setString(1, DatatypeCheck.validateType(tl.getLiteral(), tl.getDatatype()));
				addStatement.setString(2, tl.getDatatype().getIRI().toString());
				addStatement.setNull(3, Types.LONGVARCHAR);
			} else if (o instanceof OWLStringLiteral) {
				OWLStringLiteral sl = (OWLStringLiteral) o;
				addStatement.setString(1, sl.getLiteral());
				addStatement.setString(2, OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI().toString());
				addStatement.setString(3, sl.getLang());
			}
				
			addStatement.executeUpdate();		
		} catch (SQLException e) {
			e.printStackTrace();			
		}
		reasonProcessor.add(new AdditionReason(this));
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			//max 4 quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "(" +
					" id BIGINT DEFAULT nextval('uid') NOT NULL," +
					" literal TEXT," +
					" colClass TEXT," +
					" language TEXT," +
					" sourceId1 BIGINT," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 BIGINT," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 BIGINT," +
					" sourceTable3 VARCHAR(100)," +
					" sourceId4 BIGINT," +
					" sourceTable4 VARCHAR(100)," +
					" PRIMARY KEY (id, literal, colClass));" +
					" CREATE INDEX " + getDeltaName(id) + "_literal ON " + getDeltaName(id) + "(literal);" +
					" CREATE INDEX " + getDeltaName(id) + "_class ON " + getDeltaName(id) + "(colClass);");
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
			rows = stmt.executeUpdate("DELETE FROM " + delta.getDeltaName() + " AS t1 WHERE EXISTS (SELECT literal, colClass FROM " + getTableName() + " AS bottom WHERE bottom.literal = t1.literal AND bottom.colClass = t1.colClass)");
			
			
			//put delta in main table
			rows = stmt.executeUpdate("INSERT INTO " + getTableName() + " (id, literal, colClass) " +
					" SELECT MIN(id), literal, colClass " +
					" FROM " + delta.getDeltaName() +
					" GROUP BY literal, colClass");

			//if here rows are added to the main table then, genuine facts have been added
			if (rows > 0) {
				
				//save history
				if (settings.getDeletionType() == DeletionType.CASCADING) {
					String sql = null;
					
					for (int i=1; i<=4; ++i) {
						//remove rows without history
						sql = "DELETE FROM " + delta.getDeltaName() + " WHERE sourceId" + i + " IS NULL";
						rows = stmt.executeUpdate(sql);				
						
						//subjectSource
						sql = "SELECT id, '" + RelationName.classAssertionLit + "' AS colTable, sourceId" + i + ", sourceTable" + i + " FROM " + delta.getDeltaName();
						relationManager.addHistory(sql);
					}

					

				}
				
				//fire reason
				logger.debug("Relation (" + toString()  + ") has got new data");
				Reason r = new AdditionReason(this, delta);
				reasonProcessor.add(r);
			}
			
			isDirty = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		throw new U2R3NotImplementedException();
	}


	@Override
	protected String existsImpl(String... args) {
		if (args.length == 1) {
			return "SELECT literal FROM classAssertionLit WHERE literal = '" + args[0] + "'";
		} else {
			return "SELECT literal, colClass FROM classAssertionLit WHERE literal = '" + args[0] + "' AND colClass = '" + args[1] + "'";
		}
	}

}
