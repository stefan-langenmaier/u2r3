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
	private boolean startClean = true;
	
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
}
