package fr.improve.xdi.encutils;


/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class Iterators implements Iterator {
	Iterator[] iterators;
	int itCount;

	public Iterators(Iterator[] in_iterators) {
		super();
		itCount = 0;
		iterators = in_iterators;
	}

    @Override
	public void remove() {
		iterators[itCount].remove();
	}
    
    @Override
	public boolean hasNext() {
		boolean hasNext = iterators[itCount].hasNext();

		if (!hasNext && (++itCount < iterators.length)) {
			hasNext = iterators[itCount].hasNext();
		}

		return hasNext;
	}
    
    @Override
	public Object next() {
		return iterators[itCount].next();
	}
    
    @Override
	public void reset() {
		itCount = 0;

		for (int i = 0; i < iterators.length; i++) {
			iterators[i].reset();
		}
	}
}
