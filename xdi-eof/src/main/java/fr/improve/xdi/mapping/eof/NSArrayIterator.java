package fr.improve.xdi.mapping.eof;

import java.util.Enumeration;
import java.util.ListIterator;

import com.webobjects.foundation.NSArray;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class NSArrayIterator implements ListIterator {
	private NSArray _array;
	private Enumeration _enum;
	
	public NSArrayIterator(NSArray in_array) {
		_array = in_array;
		_enum = _array.objectEnumerator();
	}

	public void remove() {

	}

	public boolean hasNext() {
		return _enum.hasMoreElements();
	}

	public Object next() {
		return NSArraySet.getObject(_enum.nextElement());
	}

	/**
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	public void add(Object o) {
	}

	/**
	 * @see java.util.ListIterator#hasPrevious()
	 */
	public boolean hasPrevious() {
		return false;
	}

	/**
	 * @see java.util.ListIterator#nextIndex()
	 */
	public int nextIndex() {
		return 0;
	}

	/**
	 * @see java.util.ListIterator#previous()
	 */
	public Object previous() {
		return null;
	}

	/**
	 * @see java.util.ListIterator#previousIndex()
	 */
	public int previousIndex() {
		return 0;
	}

	/**
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	public void set(Object o) {

	}

}
