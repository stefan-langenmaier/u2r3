package de.langenmaier.u2r3.tests.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.ReasonProcessor;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.rules.TransSubClassRule;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Reason;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

/**
 * Processes axioms that are stored in a database. So it assumes tables and content are set up.
 * @author stefan
 *
 */
public class ProcessOntology {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Settings.startClean(false);
		Settings.setDeltaIteration(DeltaIteration.IMMEDIATE);
		Settings.setDeletionType(DeletionType.CLEAN);
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		
		RuleManager.initialize();
		
		ReasonProcessor rp = ReasonProcessor.getReasonProcessor();
		Reason r = new AdditionReason(RelationManager.getRelation(RelationName.subClass));
		rp.add(r);
		
		rp.classify();
	}

}
