package de.langenmaier.u2r3.util;

/**
 * Settings manages the state/mode in which U2R3 is running.
 * @author stefan
 *
 */
public class Settings {
	/**
	 * If startClean is set to true a run of U2R3 will delete all old data and start from scratch.
	 */
	private static boolean startClean = true;
	
	/**
	 * DeltaIteration describes what kinds of delta-Iterations are available.
	 * The differences of the modes can be looked up in the wiki.
	 *
	 */
	public enum DeltaIteration { COLLECTIVE, IMMEDIATE };
	private static DeltaIteration deltaIteration = DeltaIteration.IMMEDIATE;
	
	/**
	 * ConsistencyLevel describes how and how much of the consistency rules are  applied.
	 *
	 */
	public enum ConsistencyLevel { NONE, DEFAULT };
	private static ConsistencyLevel consistencyLevel = ConsistencyLevel.DEFAULT;
	
	/**
	 * InconsistencyReaction describes possible reaction on a found inconsistency.
	 *
	 */
	public enum InconsistencyReaction { WARN, FAIL };
	private static InconsistencyReaction inconsistencyReaction = InconsistencyReaction.WARN;
	
	/**
	 * DeletionType describes how the deletion of an axiom should be handled.
	 */
	public enum DeletionType { CLEAN, CASCADING };
	private static DeletionType deletionType = DeletionType.CASCADING;
	
	/**
	 * EvaluationStrategy describes which Rules should be deferred
	 * to get executed.
	 */
	public enum EvaluationStrategy { COMMONLAST, RARELAST };
	private static EvaluationStrategy evaluationStrategy = EvaluationStrategy.COMMONLAST;
	
	public static void startClean(boolean sc) {
		startClean = sc;
	}
	
	public static boolean startClean() {
		return startClean;
	}

	public static DeltaIteration getDeltaIteration() {
		return deltaIteration;
	}

	public static void setDeltaIteration(DeltaIteration deltaIteration) {
		Settings.deltaIteration = deltaIteration;
	}

	public static ConsistencyLevel getConsistencyLevel() {
		return consistencyLevel;
	}

	public static void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
		Settings.consistencyLevel = consistencyLevel;
	}

	public static InconsistencyReaction getInconsistencyReaction() {
		return inconsistencyReaction;
	}

	public static void setInconsistencyReaction(
			InconsistencyReaction inconsistencyReaction) {
		Settings.inconsistencyReaction = inconsistencyReaction;
	}

	public static void setDeletionType(DeletionType deletionType) {
		Settings.deletionType = deletionType;
	}

	public static DeletionType getDeletionType() {
		return deletionType;
	}

	public static void setEvaluationStrategy(EvaluationStrategy evaluationStrategy) {
		Settings.evaluationStrategy = evaluationStrategy;
	}

	public static EvaluationStrategy getEvaluationStrategy() {
		return evaluationStrategy;
	}
}
