package de.langenmaier.u2r3.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Settings manages the state/mode in which U2R3 is running.
 * @author stefan
 *
 */
public class Settings {
	static Logger logger = Logger.getLogger(Settings.class);
	
	/**
	 * Loads the default configuration from the file u2r3.properties
	 */
	public Settings() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("u2r3.properties"));
			
			if (prop.containsKey("u2r3.consistencyLevel")) {
				if (prop.get("u2r3.consistencyLevel").equals("NONE")) {
					setConsistencyLevel(ConsistencyLevel.NONE);
				} else if (prop.get("u2r3.consistencyLevel").equals("DEFAULT")) {
					setConsistencyLevel(ConsistencyLevel.DEFAULT);
				}
			}
			
			if (prop.containsKey("u2r3.deltaIteration")) {
				if (prop.get("u2r3.deltaIteration").equals("COLLECTIVE")) {
					setDeltaIteration(DeltaIteration.COLLECTIVE);
				} else if (prop.get("u2r3.deltaIteration").equals("IMMEDIATE")) {
					setDeltaIteration(DeltaIteration.IMMEDIATE);
				}
			}
			
			if (prop.containsKey("u2r3.inconsistencyReaction")) {
				if (prop.get("u2r3.inconsistencyReaction").equals("WARN")) {
					setInconsistencyReaction(InconsistencyReaction.WARN);
				} else if (prop.get("u2r3.inconsistencyReaction").equals("FAIL")) {
					setInconsistencyReaction(InconsistencyReaction.WARN);
				}
			}
			
			if (prop.containsKey("u2r3.deletionType")) {
				if (prop.get("u2r3.deletionType").equals("CASCADING")) {
					setDeletionType(DeletionType.CASCADING);
				} else if (prop.get("u2r3.deletionType").equals("CLEAN")) {
					setDeletionType(DeletionType.CLEAN);
				}
			}
			
			if (prop.containsKey("u2r3.evaluationStrategy")) {
				if (prop.get("u2r3.evaluationStrategy").equals("COMMONLAST")) {
					setEvaluationStrategy(EvaluationStrategy.COMMONLAST);
				} else if (prop.get("u2r3.evaluationStrategy").equals("RARELAST")) {
					setEvaluationStrategy(EvaluationStrategy.RARELAST);
				}
			}
			
			if (prop.containsKey("u2r3.checkProfile")) {
				if (prop.get("u2r3.checkProfile").equals("TRUE")) {
					checkProfile(true);
				} else if (prop.get("u2r3.checkProfile").equals("FALSE")) {
					checkProfile(false);
				}
			}
			
			if (prop.containsKey("u2r3.databaseMode")) {
				if (prop.get("u2r3.databaseMode").equals("EMBEDDED")) {
					setDatabaseMode(DatabaseMode.EMBEDDED);
				} else if (prop.get("u2r3.databaseMode").equals("IN_MEMORY")) {
					setDatabaseMode(DatabaseMode.IN_MEMORY);
				} else if (prop.get("u2r3.databaseMode").equals("STANDALONE")) {
					setDatabaseMode(DatabaseMode.STANDALONE);
				}
			}
		} catch (FileNotFoundException e) {

			logger.warn("No configuration file found! Using default values.");
		} catch (IOException e) {
			logger.error("IO Error");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * If startClean is set to true a run of U2R3 will delete all old data and start from scratch.
	 */
	private boolean startClean = true;
	
	/**
	 * Database Mode describes how the database is started.
	 *
	 */
	public enum DatabaseMode { STANDALONE, EMBEDDED, IN_MEMORY };
	private DatabaseMode databaseMode = DatabaseMode.EMBEDDED;
	
	/**
	 * DeltaIteration describes what kinds of delta-Iterations are available.
	 * The differences of the modes can be looked up in the wiki.
	 *
	 */
	public enum DeltaIteration { COLLECTIVE, IMMEDIATE };
	private DeltaIteration deltaIteration = DeltaIteration.IMMEDIATE;
	
	/**
	 * ConsistencyLevel describes how and how much of the consistency rules are  applied.
	 *
	 */
	public enum ConsistencyLevel { NONE, DEFAULT };
	private ConsistencyLevel consistencyLevel = ConsistencyLevel.DEFAULT;
	
	/**
	 * InconsistencyReaction describes possible reaction on a found inconsistency.
	 *
	 */
	public enum InconsistencyReaction { WARN, FAIL };
	private InconsistencyReaction inconsistencyReaction = InconsistencyReaction.WARN;
	
	/**
	 * DeletionType describes how the deletion of an axiom should be handled.
	 */
	public enum DeletionType { CLEAN, CASCADING };
	private DeletionType deletionType = DeletionType.CASCADING;
	
	/**
	 * EvaluationStrategy describes which Rules should be deferred
	 * to get executed.
	 */
	public enum EvaluationStrategy { COMMONLAST, RARELAST };
	private EvaluationStrategy evaluationStrategy = EvaluationStrategy.COMMONLAST;
	
	
	/**
	 * Should a loaded ontology be checked if it is in the RL Profile
	 */
	private boolean checkProfile = true;
	
	public void startClean(boolean sc) {
		startClean = sc;
	}
	
	public boolean startClean() {
		return startClean;
	}

	public DeltaIteration getDeltaIteration() {
		return deltaIteration;
	}

	public void setDeltaIteration(DeltaIteration deltaIteration) {
		this.deltaIteration = deltaIteration;
	}

	public ConsistencyLevel getConsistencyLevel() {
		return consistencyLevel;
	}

	public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
	}

	public InconsistencyReaction getInconsistencyReaction() {
		return inconsistencyReaction;
	}

	public void setInconsistencyReaction(
			InconsistencyReaction inconsistencyReaction) {
		this.inconsistencyReaction = inconsistencyReaction;
	}

	public void setDeletionType(DeletionType deletionType) {
		this.deletionType = deletionType;
	}

	public DeletionType getDeletionType() {
		return deletionType;
	}

	public void setEvaluationStrategy(EvaluationStrategy evaluationStrategy) {
		this.evaluationStrategy = evaluationStrategy;
	}

	public EvaluationStrategy getEvaluationStrategy() {
		return evaluationStrategy;
	}

	public void checkProfile(boolean b) {
		checkProfile = b;
	}
	
	public boolean checkProfile() {
		return checkProfile;
	}

	public void setDatabaseMode(DatabaseMode databaseMode) {
		this.databaseMode = databaseMode;
	}

	public DatabaseMode getDatabaseMode() {
		return databaseMode;
	}
}
