package fr.improve.xdi.encutils;

import java.util.Collection;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class ResetIterator implements Iterator {
	java.util.Iterator _iterator;
	Collection _collection;

	public ResetIterator(Collection in_collection) {
		_collection = in_collection;
		_iterator = in_collection.iterator();
	}
    
    @Override
	public void reset() {
		_iterator = _collection.iterator();
	}
    
    @Override
	public void remove() {
		_iterator.remove();
	}
    
    @Override
	public boolean hasNext() {
		return _iterator.hasNext();
	}
    
    @Override
	public Object next() {
		return _iterator.next();
	}

}
