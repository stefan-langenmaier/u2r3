package de.langenmaier.u2r3.util;

import java.util.HashMap;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLObject;

import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;

public class NodeIDMapper extends HashMap<OWLObject, NodeID> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3972777156248576738L;
	
	/**
	 * When the ID for an object is asked you always get an answer
	 * cause it will be generated upon request.
	 */
	@Override
	public NodeID get(Object key) {
		if (!(key instanceof OWLObject)) {
			throw new U2R3NotImplementedException();
		}
		OWLObject oo = (OWLObject) key;
		if (!containsKey(key)) {
			put(oo, NodeID.getNodeID());
		}
		return super.get(key);
	}

}
