package fr.improve.xdi.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EnterpriseObjectCollection extends ArrayList {

	/**
	 *
	 */
	public EnterpriseObjectCollection() {
		super();
	}

	/**
	 *
	 */
	public EnterpriseObjectCollection(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 *
	 */
	public EnterpriseObjectCollection(Collection c) {
		super(c);
	}

	/**
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		return new EnterpriseObjectIterator(super.iterator());
	}

}
