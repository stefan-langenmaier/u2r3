package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3ReasonerException;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.TableId;
import de.langenmaier.u2r3.util.U2R3Component;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

/**
 * Contains the default methods that a relation should contain and tries
 * to do much of the default connection stuff.
 * @author stefan
 *
 */
public abstract class Relation extends U2R3Component {
	static Logger logger = Logger.getLogger(Relation.class);
	
	protected enum AdditionMode {ADD, NOADD};
	
	protected Connection conn = null;
	protected PreparedStatement addStatement;
	protected PreparedStatement createMainStatement;
	protected PreparedStatement dropMainStatement;
	protected Statement createDeltaStatement;
	protected Statement dropDeltaStatement;
	protected PreparedStatement addListStatement;

	
	private int nextDelta = 0;
	private HashMap<Integer, DeltaRelation> deltas = new HashMap<Integer, DeltaRelation>();
	
	protected String tableName;
	
	//rules that should be triggered when something is added to the relation
	protected HashSet<Rule> additionRules = new HashSet<Rule>();
	
	//rules that should be triggered when something is removed from the relation
	protected HashSet<Rule> deletionRules = new HashSet<Rule>();

	protected boolean isDirty = false;
	
	protected Relation(U2R3Reasoner reasoner) {
		super(reasoner);
		conn = U2R3DBConnection.getConnection();
		try {
			createDeltaStatement = conn.createStatement();
			dropDeltaStatement = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prepares the insert statement for the facts from the axiom
	 * If the return value is false, the method has executed the
	 * statement by itself. Necessary for complex or special axioms.
	 * @param axiom
	 * @return true the statement needs to be executed otherwise not
	 * @throws SQLException
	 */
	public abstract AdditionMode addImpl(OWLAxiom axiom) throws SQLException;
	
	public void add(OWLAxiom axiom) {
		try {
			if (addImpl(axiom) == AdditionMode.ADD) {
				logger.trace(addStatement.toString());
				addStatement.executeUpdate();
				reasonProcessor.add(new AdditionReason(this));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void add(OWLObject o) {
		throw new U2R3NotImplementedException();
	}
	
	//TODO return type auf void umstellen
	public abstract Pair<UUID, RelationName> removeImpl(OWLAxiom axiom) throws SQLException;
	
	public void remove(OWLAxiom axiom) {
		try {
			reasonProcessor.pause();
			
			removeImpl(axiom);
			
			//Pair<UUID, RelationName> res = removeImpl(axiom);

//			if (res != null && res.getFirst() != null) {
//				relationManager.remove(res.getFirst(), res.getSecond());
//			}
			
			reasonProcessor.resume();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void remove(OWLObject o) {
		throw new U2R3NotImplementedException();
	}
	
	public HashSet<Rule> getAdditionRules() {
		return additionRules;
	}
	
	public void addAdditionRule(Rule rule) {
		additionRules.add(rule);
	}
	
	public HashSet<Rule> getDeletionRules() {
		return deletionRules;
	}
	
	public void addDeletionRule(Rule rule) {
		deletionRules.add(rule);
	}
	
	protected void create() {
		try {
			if (settings.startClean()) {
				dropMainStatement.execute();
				createMainStatement.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void createDeltaImpl(int id);
	
	private void createDelta(int id) {
			if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
				++nextDelta;
			}
			createDeltaImpl(id);
	}
	
	protected void dropDelta(int id) {
		try {
			dropDeltaStatement.execute("DROP TABLE " + getDeltaName(id) + " IF EXISTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected synchronized int getNewDelta() {
		return nextDelta;
	}
	
	protected synchronized int getDelta() {
		return nextDelta-1;
	}

	/**
	 * Should only be used in the collective Mode
	 */
	public void makeDirty() {
		if (settings.getDeltaIteration() == DeltaIteration.IMMEDIATE) {
			throw new RuntimeException("this is not allowed in immediate mode");
		}
		isDirty = true;
		
	}
	
	/**
	 * The delta Relation is merged to the main relation
	 * @param delta
	 */
	public abstract void merge(DeltaRelation delta);
		
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Merges the current delta to the main relation
	 */
	public void merge() {
		merge(deltas.get(getNewDelta()));
		if (getDelta() != DeltaRelation.NO_DELTA) {
			dropDelta(getDelta());
		}
		++nextDelta;
	}

	protected String getTableName() {
		return tableName;
	}
	
	protected String getDeltaName(int delta) {
		return getDeltaName(delta, tableName);
	}
	
	public DeltaRelation createDeltaRelation(int delta) {
		if (!deltas.containsKey(delta)) {
			if (delta != DeltaRelation.NO_DELTA) {
				createDelta(delta);
			}
			deltas.put(delta, new DeltaRelation(this, delta));
		}
		DeltaRelation deltaRelation = deltas.get(delta);
		return deltaRelation;
	}
	
	public DeltaRelation createNewDeltaRelation() {
		return createDeltaRelation(getNewDelta());
	}

	public void removeDeltaRelation(int delta) {
		dropDelta(delta);
		deltas.remove(delta);
	}
	protected abstract String existsImpl(String... args);
	
	public boolean exists(String... args) throws U2R3ReasonerException {
		try {
			Statement stmt = conn.createStatement();
			String sql = existsImpl(args);
			
			return stmt.executeQuery(sql).next();
		} catch (SQLException e) {
			throw new U2R3ReasonerException(e);
		}
	}
	
	protected void handleAnonymousClassExpression(OWLClassExpression ce) {
		if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
			relationManager.getRelation(RelationName.complementOf).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSETION_OF) {
			relationManager.getRelation(RelationName.intersectionOf).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
			relationManager.getRelation(RelationName.unionOf).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
			relationManager.getRelation(RelationName.oneOf).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			relationManager.getRelation(RelationName.someValuesFrom).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
			relationManager.getRelation(RelationName.allValuesFrom).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_VALUE) {
			relationManager.getRelation(RelationName.hasValueEnt).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
			OWLObjectMaxCardinality mc = (OWLObjectMaxCardinality) ce;
			if (mc.isQualified()) {
				relationManager.getRelation(RelationName.maxQualifiedCardinality).add(ce);
			} else {
				relationManager.getRelation(RelationName.maxCardinality).add(ce);
			}
		} else {
			throw new U2R3NotImplementedException();
		}
	}
	
	protected void handleAnonymousObjectPropertyExpression(OWLObjectPropertyExpression pe) {
		//Dürfen laut Grammatik nur für InverseObjectProperty aufgerufen werden
		//ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
		throw new U2R3NotImplementedException();
	}
	
	protected void handleAnonymousDataPropertyExpression(OWLDataPropertyExpression pe) {
		//Dürfen laut Grammatik auch niemals aufgerufen werden
		//DataPropertyExpression := DataProperty
		throw new U2R3NotImplementedException();
	}
	
	protected void handleAnonymousIndividual(OWLIndividual ind) {
		//Dürfen laut Grammatik auch niemals aufgerufen werden
		//AnonymousIndividual := nodeID
		throw new U2R3NotImplementedException();
	}
	
	protected void getSubSQL(StringBuilder sql, OWLClassExpression ce, String tid, String col) {
		if (ce.isAnonymous()) {
			String ntid = TableId.getId();
			if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSETION_OF) {
				OWLObjectIntersectionOf oi = (OWLObjectIntersectionOf) ce;
				//ntid = TableId.getId();
				String ltid;
				
				sql.append("SELECT class");
				sql.append("\n FROM intersectionOf AS " + ntid);
				sql.append("\nWHERE " + ntid + ".class = " + tid +"." + col);
				for(OWLClassExpression sce : oi.getOperands()) {
					ltid = TableId.getId();
					sql.append(" AND ");
					sql.append(" EXISTS (");
					sql.append("\nSELECT element");
					sql.append("\nFROM list AS " + ltid);
					sql.append("\nWHERE " + ltid +".name = " + ntid + ".list");
					sql.append(" AND EXISTS (");
					getSubSQL(sql, sce, ltid, "element");
					sql.append("))"); 
				}
			} else if(ce.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) ce;
				sql.append("SELECT part");
				sql.append("\n FROM someValuesFrom AS " + ntid);
				sql.append("\nWHERE EXISTS (");
				getSubSQL(sql, svf.getProperty(), ntid, "property");
				sql.append(") AND EXISTS (");
				getSubSQL(sql, svf.getFiller(), ntid, "total");
				sql.append(")");
			} else if(ce.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
				OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) ce;
				sql.append("SELECT part");
				sql.append("\n FROM allValuesFrom AS " + ntid);
				sql.append("\nWHERE EXISTS (");
				getSubSQL(sql, avf.getProperty(), ntid, "property");
				sql.append(") AND EXISTS (");
				getSubSQL(sql, avf.getFiller(), ntid, "total");
				sql.append(")");
			} else {
				sql.append("\nXXXXXXX\nTODO CE:" + ce.getClassExpressionType() + "\n");
				throw new U2R3NotImplementedException();
			}
		} else {
			sql.append("SELECT '");
			sql.append(ce.asOWLClass().getIRI().toString());
			sql.append("'");
			sql.append("\n WHERE '");
			sql.append(ce.asOWLClass().getIRI().toString());
			sql.append("' = ");
			sql.append(tid + "." + col);
		}
	}


	protected void getSubSQL(StringBuilder sql,
			OWLObjectPropertyExpression property, String tid, String col) {
		if (property.isAnonymous()) {
			sql.append("\nXXXXXXX\nTODO CE:" + property.toString() + "\n");
			throw new U2R3NotImplementedException();
		} else {
			sql.append("SELECT '" + property.asOWLObjectProperty().getIRI().toString() + "'");
			sql.append("\n WHERE '");
			sql.append(property.asOWLObjectProperty().getIRI().toString());
			sql.append("' = ");
			sql.append(tid + "." + col);
		}
		
	}

	protected void getSubSQL(StringBuilder sql, OWLIndividual individual, String tid, String col) {
		if (individual.isAnonymous()) {
			sql.append("SELECT '" + individual.asAnonymousIndividual().getID().toString() + "'");
		} else {
			sql.append("SELECT '" + individual.asNamedIndividual().getIRI().toString() + "'");
			sql.append("\n WHERE '");
			sql.append(individual.asNamedIndividual().getIRI().toString());
			sql.append("' = ");
			sql.append(tid + "." + col);
		}
	}


	public String getDeltaName(int delta, String table) {
		if (!table.equals(tableName)) return table;
		if (delta == DeltaRelation.NO_DELTA) {
			return getTableName();
		}
		return getTableName() + "_d" + delta;
	}
	
	protected void removeAnonymousClassExpression(OWLClassExpression ce) {
		if(ce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSETION_OF) {
			relationManager.getRelation(RelationName.intersectionOf).remove(ce);
		} else if(ce.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			relationManager.getRelation(RelationName.someValuesFrom).remove(ce);
		} else if(ce.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
			relationManager.getRelation(RelationName.allValuesFrom).remove(ce);
		} else {
			System.out.println(ce.getClassExpressionType());
			throw new U2R3NotImplementedException();
		}
		
	}
	
	protected void removeAnonymousPropertyExpression(OWLObjectPropertyExpression property) {
		throw new U2R3NotImplementedException();
	}
	
	protected void removeAnonymousIndividual(OWLIndividual subject) {
		throw new U2R3NotImplementedException();
	}
}
