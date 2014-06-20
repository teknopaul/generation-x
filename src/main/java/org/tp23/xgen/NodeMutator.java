package org.tp23.xgen;

import org.w3c.dom.Node;

/**
 * Apply a function to each node in a XGenNodeList
 * @author teknopaul
 */
public interface NodeMutator {

	/**
	 * @param node Node to be mutated
	 * @return tail node for chaining.
	 */
	public Node each(Node node);
	
}
