package fr.improve.xdi.mapping.eof;

import com.webobjects.eocontrol.EOSortOrdering;

import fr.improve.xdi.mapping.SortOrdering;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class SortOrderingImpl implements SortOrdering {
	private String _key = null;
	private int _type = 0;
	private EOSortOrdering _order = null;

	/**
	 * 
	 */
	public SortOrderingImpl(String in_key, int in_type) {
		switch(in_type) {
			case SortOrdering.ASC : _order = new EOSortOrdering(in_key, EOSortOrdering.CompareAscending);break;
			case SortOrdering.DESC : _order = new EOSortOrdering(in_key, EOSortOrdering.CompareDescending);break;
		}
		_key = in_key;
		_type = in_type;
	}

	/* 
	 * @see fr.improve.xdi.mapping.SortOrdering#getKey()
	 */
	public String getKey() {
		return _key;
	}

	/* 
	 * @see fr.improve.xdi.mapping.SortOrdering#getType()
	 */
	public int getType() {
		return _type;
	}

	/* 
	 * @see fr.improve.xdi.mapping.SortOrdering#getOrder()
	 */
	public Object getOrder() {
		return _order;
	}

}
