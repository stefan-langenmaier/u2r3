package de.langenmaier.u2r3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.rules.Rule;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.U2R3Component;

/**
 * Contains the default methods that a relation should contain and tries
 * to do much of the default connection stuff.
 * @author stefan
 *
 */
public abstract class Relation extends U2R3Component implements Query {
	static Logger logger = Logger.getLogger(Relation.class);
	
	protected enum AdditionMode {ADD, NOADD};
	
	protected Connection conn = null;
	protected PreparedStatement addStatement;
	protected PreparedStatement createMainStatement;
	protected PreparedStatement dropMainStatement;
	protected PreparedStatement addListStatement;

	protected String tableName;
	
	//rules that should be triggered when something is added to the relation
	protected HashSet<Rule> additionRules = new HashSet<Rule>();
	
	//rules that should be triggered when something is removed from the relation
	protected HashSet<Rule> deletionRules = new HashSet<Rule>();

	protected boolean isDirty = false;
	
	protected Relation(U2R3Reasoner reasoner) {
		super(reasoner);
		conn = U2R3DBConnection.getConnection();
		
	}
	
	/**
	 * Prepares the insert statement for the facts from the axiom
	 * If the return value is false, the method has executed the
	 * statement by itself. Necessary for complex or special axioms.
	 * @param axiom
	 * @return true the statement needs to be executed otherwise not
	 * @throws SQLException
	 */
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();
	}
	
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
	
	
	/**
	 * Methode die Unterkonstrukte hinzufügt
	 * @param o
	 */
	public void add(OWLObject o) {
		throw new U2R3NotImplementedException();
	}
	
	/**
	 * Has to be implemented by every relation. The axiom is already removed
	 * but it has to remove recursive all of the complex parts of the axiom.
	 * @param axiom
	 * @throws SQLException
	 */
	public void removeImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();
	}
	
	public void remove(OWLAxiom axiom) {
		try {
			reasonProcessor.pause();
			
			ResultSet rs = getAxiomLocation(axiom).executeQuery();
			
			if (rs.next()) {
				relationManager.remove(rs.getLong("id"), RelationName.valueOf(rs.getString("colTable")));
			}
			
			removeImpl(axiom);
			
			reasonProcessor.resume();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void removeObject(OWLClassExpression classExpression) throws SQLException {
		ResultSet rs;
		Statement stmt = conn.createStatement();
		StringBuilder sql = new StringBuilder();
		
		handleSubAxiomLocationImpl(sql, classExpression, null, null);
		rs = stmt.executeQuery(sql.toString());
		
		if (rs.next()) {
			relationManager.remove(rs.getLong("id"), RelationName.valueOf(rs.getString("colTable")));
		}
	}


	protected void removeObject(OWLIndividual individual) throws SQLException {
		ResultSet rs;
		Statement stmt = conn.createStatement();
		StringBuilder sql = new StringBuilder();
		
		handleSubAxiomLocationImpl(sql, individual, null, null);
		rs = stmt.executeQuery(sql.toString());
		
		if (rs.next()) {
			relationManager.remove(rs.getLong("id"), RelationName.valueOf(rs.getString("colTable")));
		}
		
		removeImpl(individual);
	}
	
	protected void removeObject(OWLObjectPropertyExpression pe) throws SQLException {
		ResultSet rs;
		Statement stmt = conn.createStatement();
		StringBuilder sql = new StringBuilder();
		
		handleSubAxiomLocationImpl(sql, pe, null, null);
		rs = stmt.executeQuery(sql.toString());
		
		if (rs.next()) {
			relationManager.remove(rs.getLong("id"), RelationName.valueOf(rs.getString("colTable")));
		}
		
		removeImpl(pe);
	}
	
	/**
	 * Removes a "sub" object from an axiom. This works recursive and deletes
	 * all of the reasoning history.
	 * @param o
	 */
	protected void removeImpl(OWLObject o) {
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
		if (settings.startClean()) {
			try {
				dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());
				dropMainStatement.execute();
			} catch (SQLException e) {
				logger.warn("Relation '" + getTableName() + "' has NOT been deleted");
				//e.printStackTrace();
			}
			try {
				createMainStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	protected String getTableName() {
		return tableName;
	}

	
	/**
	 * @param ce
	 */
	protected void handleAddAnonymousClassExpression(OWLClassExpression ce) {
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
		} else if (ce.getClassExpressionType() == ClassExpressionType.DATA_HAS_VALUE) {
			relationManager.getRelation(RelationName.hasValueLit).add(ce);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
			OWLObjectMaxCardinality mc = (OWLObjectMaxCardinality) ce;
			if (mc.isQualified()) {
				relationManager.getRelation(RelationName.maxQualifiedCardinality).add(ce);
			} else {
				relationManager.getRelation(RelationName.maxCardinality).add(ce);
			}
		} else {
			System.out.println(ce.getClassExpressionType());
			throw new U2R3NotImplementedException();
		}
	}
	
	protected void handleAddAnonymousObjectPropertyExpression(OWLObjectPropertyExpression pe) {
		//Dürfen laut Grammatik nur für InverseObjectProperty aufgerufen werden
		//ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
		if (pe instanceof OWLObjectInverseOf) {
			relationManager.getRelation(RelationName.inverseOf).add(pe);
		} else {
			throw new U2R3NotImplementedException();
		}
		
	}
	
	protected void handleAddAnonymousDataPropertyExpression(OWLDataPropertyExpression pe) {
		//Dürfen laut Grammatik auch niemals aufgerufen werden
		//DataPropertyExpression := DataProperty
		throw new U2R3NotImplementedException();
	}
	
	protected void handleAddAnonymousIndividual(OWLIndividual ind) {
		//Dürfen laut Grammatik auch niemals aufgerufen werden
		//AnonymousIndividual := nodeID
		throw new U2R3NotImplementedException();
	}
	
	/* ***********************************************************************
	 *                             Query Interface                           *
	 *************************************************************************/
	@Override
	public PreparedStatement getAxiomLocation(OWLAxiom ax) throws SQLException {
		throw new U2R3NotImplementedException();
	}
	
	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col) {
		throw new U2R3NotImplementedException();
	}
	
	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLObjectPropertyExpression pe, String tid, String col) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLDataPropertyExpression pe, String tid, String col) {
		//Sollte nie passieren
		throw new U2R3NotImplementedException();
	}

	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLIndividual ind, String tid, String col) {
		//Sollte nie passieren
		throw new U2R3NotImplementedException();
	}

	@Override
	public final void handleSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col) {
		sql.append("(");
		
		if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSETION_OF) {
			relationManager.getRelation(RelationName.intersectionOf).getSubAxiomLocationImpl(sql, ce, tid, col);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			relationManager.getRelation(RelationName.someValuesFrom).getSubAxiomLocationImpl(sql, ce, tid, col);
		} else if (ce.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
			relationManager.getRelation(RelationName.allValuesFrom).getSubAxiomLocationImpl(sql, ce, tid, col);
		} else {
			throw new U2R3NotImplementedException();
		}
		
		sql.append(")");
	}

	@Override
	public final void handleSubAxiomLocationImpl(StringBuilder sql, OWLObjectPropertyExpression pe, String tid, String col) {
		if (pe instanceof OWLObjectInverseOf) {
			sql.append("(");
			relationManager.getRelation(RelationName.inverseOf).getSubAxiomLocationImpl(sql, pe, tid, col);
			sql.append(")");
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public final void handleSubAxiomLocationImpl(StringBuilder sql, OWLDataPropertyExpression pe, String tid, String col) {
		//Sollte nie passieren
		throw new U2R3NotImplementedException();
	}

	@Override
	public final void handleSubAxiomLocationImpl(StringBuilder sql, OWLIndividual ind, String tid, String col) {
		//Sollte nie passieren
		throw new U2R3NotImplementedException();
	}
}
