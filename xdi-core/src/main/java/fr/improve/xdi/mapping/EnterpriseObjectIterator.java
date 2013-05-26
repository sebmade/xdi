package fr.improve.xdi.mapping;

import java.util.Iterator;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class EnterpriseObjectIterator implements Iterator {
	private Iterator _iterator;

	/**
	 *
	 */
	public EnterpriseObjectIterator(Iterator in_iterator) {
		_iterator = in_iterator;
	}

	/**
	 * @see java.util.Iterator#remove()
	 */    
    @Override
	public void remove() {
		_iterator.remove();
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */    
    @Override
	public boolean hasNext() {
		return _iterator.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */    
    @Override
	public Object next() {
		Object l_obj = _iterator.next();
		if (l_obj instanceof EnterpriseObject) {
			return ((EnterpriseObject)l_obj).baseObject();
		}
		return l_obj;
	}

}
