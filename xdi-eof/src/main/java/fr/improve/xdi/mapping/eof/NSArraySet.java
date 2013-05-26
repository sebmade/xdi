package fr.improve.xdi.mapping.eof;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import fr.improve.xdi.mapping.EnterpriseObject;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class NSArraySet<E> implements Set<E>, List<E> {
	private NSArray _array;
	
	public NSArraySet(NSArray in_array) {
		_array = in_array;
	}
	
	public static Object getObject(Object in_object) {
		if (in_object instanceof EOEnterpriseObject) {
			return new EnterpriseObjectImpl((EOEnterpriseObject)in_object);
		} else if (in_object instanceof EnterpriseObject) {
			return ((EnterpriseObject)in_object).baseObject();
		}
		return in_object;
	}

	public static Object[] getObjects(Collection in_objects) {
		Iterator it = in_objects.iterator();
		Object[] l_result = new Object[in_objects.size()];
		int i = 0;
		while (it.hasNext()) {
			l_result[i++] = getObject(it.next());
		}
		return l_result;
	}
	
	/*
	 * Set Interface Implementation
	 */
	 
	public int size() {
		return _array.count();
	}

	public void clear() {
		if (_array instanceof NSMutableArray) {
			((NSMutableArray)_array).removeAllObjects();
		}
	}

	public boolean isEmpty() {
		return (_array.count() == 0);
	}

	public Object[] toArray() {
		return _array.objects();
	}

	public boolean add(Object o) {
		if (_array instanceof NSMutableArray) {
			((NSMutableArray)_array).addObject(getObject(o));
			return true;
		}
		return false;
	}

	public boolean contains(Object o) {
		return _array.containsObject(getObject(o));
	}

	public boolean remove(Object o) {
		if (_array instanceof NSMutableArray) {
			((NSMutableArray)_array).removeObject(getObject(o));
		}
		return false;
	}

	public boolean addAll(Collection c) {
		if (_array instanceof NSMutableArray) {
			((NSMutableArray)_array).addObjects(getObjects(c));
		}
		return false;
	}

	public boolean containsAll(Collection c) {
		return false;
	}

	public boolean removeAll(Collection c) {
		if (_array instanceof NSMutableArray) {
			((NSMutableArray)_array).removeObjects(getObjects(c));
		}
		return false;
	}

	public boolean retainAll(Collection c) {
		return false;
	}

	public Iterator iterator() {
		return new NSArrayIterator(_array);
	}

	public Object[] toArray(Object[] a) {
        for (int i = 0;i<_array.count();i++) {
            a[i] = get(i);
        }
		return a;
	}

	/*
	 * List Interface Implementation
	 */
	 
	/**
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element) {
		if (_array instanceof NSMutableArray) {
			((NSMutableArray)_array).insertObjectAtIndex(getObject(element), index);
		}
	}

	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		if (_array instanceof NSMutableArray) {
			if (index >= 0 && index < size()) {
				Iterator i = c.iterator();
				while(i.hasNext()) {
					((NSMutableArray)_array).insertObjectAtIndex(getObject(i.next()), index++);
				}
			} else {
				((NSMutableArray)_array).addObjects(getObjects(c));
			}
			return true;
		}
		return false;
	}

	/**
	 * @see java.util.List#get(int)
	 */
	public E get(int index) {
		return (E) getObject(_array.objectAtIndex(index));
	}

	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return _array.indexOfObject(getObject(o));
	}

	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		int idx = -1, idxTmp = 0;
		NSRange l_range = new NSRange(0, size());
		while (idxTmp != -1) {
			idxTmp = _array.indexOfObject(getObject(o), l_range);
			l_range = new NSRange(idx+1, size()-idx);
			idx = idxTmp;
		}
		return idx;
	}

	/**
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		return new NSArrayIterator(_array);
	}

	/**
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		return null;
	}

	/**
	 * @see java.util.List#remove(int)
	 */
	public E remove(int index) {
		if (_array instanceof NSMutableArray) {
			return (E) getObject(((NSMutableArray)_array).removeObjectAtIndex(index));
		}
		return null;
	}

	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element) {
		if (_array instanceof NSMutableArray) {
			return getObject(((NSMutableArray)_array).replaceObjectAtIndex(element, index));
		}
		return null;
	}

	/**
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		return new NSArraySet(_array.subarrayWithRange(new NSRange(fromIndex, toIndex-fromIndex)));
	}

}
