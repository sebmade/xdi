package fr.improve.xdi.encutils;

import org.w3c.dom.NodeList;


/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class NodesIterator implements Iterator {
	NodeList[] lists;
	int nodeCount;
	int listCount;
	boolean hasNext;

	public NodesIterator(NodeList[] in_lists) {
		super();
		reset();
		lists = in_lists;
	}
    
    @Override
	public void remove() {}
    
    @Override
	public boolean hasNext() {
		if (listCount < lists.length) {
			if (nodeCount < lists[listCount].getLength()) {
				hasNext = true;

				return hasNext;
			}

			if (++listCount < lists.length) {
				nodeCount = 0;
				hasNext = true;

				return hasNext;
			}
		}

		hasNext = false;

		return hasNext;
	}
    
    @Override
	public Object next() {
		if (hasNext) {
			return lists[listCount].item(nodeCount++).getNodeValue();
		} else {
			return null;
		}
	}
    
    @Override
	public void reset() {
		nodeCount = 0;
		listCount = 0;
		hasNext = true;
	}
}
