package de.langenmaier.u2r3.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

import de.langenmaier.u2r3.RuleAction;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

/**
 * This class implements a specialized queue for RuleAction objects.
 * The objects are accessible in a specific way to ensure easy use in a thread pool environment.
 * Which elements are the first and the last one is dynamic.
 * 
 * @author stefan
 *
 */
public class RuleActionQueue implements Queue<RuleAction> {
	RuleActionWeightMap weights = new RuleActionWeightMap();
	RuleActionPriorityQueue priorityQueue = new RuleActionPriorityQueue();
	RuleActionDeltaMap deltas = new RuleActionDeltaMap();
	
	HashSet<RuleAction> active = new HashSet<RuleAction>();
	
	/**
	 * Returns a RuleAction object that should be processed.
	 * The objects is not instantly deleted but its removed from the queue.
	 * When the RuleAction is processed the object must be deleted with the delete method.
	 * @return
	 */
	public RuleAction activate() {
		if (!priorityQueue.isEmpty()) {
			RuleAction next = priorityQueue.remove();
			weights.remove(next);
			active.add(next);
			
			return next;
		}
		return null;
	}
	
	
	/**
	 * The RuleAction object that should be deleted must be activated before!
	 * @param ra
	 */
	public boolean delete(RuleAction ra) {
		if (deltas.reduce(ra)) {
			//ra.getDeltaRelation().clear();
			//TODO hier kann man die delta-relation löschen, da sie nicht mehr gebraucht wird
		}
		return active.remove(ra);
	}
	
	@Override
	public boolean add(RuleAction ra) {
		weights.put(ra);
		deltas.put(ra);
		priorityQueue.add(ra);
		return true;
	}

	@Override
	public RuleAction element() {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean offer(RuleAction e) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public RuleAction peek() {
		throw new U2R3NotImplementedException();
	}

	@Override
	public RuleAction poll() {
		throw new U2R3NotImplementedException();
	}

	@Override
	public RuleAction remove() {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean addAll(Collection<? extends RuleAction> c) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void clear() {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean contains(Object o) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean isEmpty() {
		return priorityQueue.isEmpty();
	}

	@Override
	public Iterator<RuleAction> iterator() {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean remove(Object o) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public int size() {
		return priorityQueue.size();
	}

	@Override
	public Object[] toArray() {
		throw new U2R3NotImplementedException();
		}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new U2R3NotImplementedException();
	}

}
