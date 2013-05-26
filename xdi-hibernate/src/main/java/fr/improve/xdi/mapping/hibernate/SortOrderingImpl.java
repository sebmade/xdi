package fr.improve.xdi.mapping.hibernate;

import org.hibernate.criterion.Order;

import fr.improve.xdi.mapping.SortOrdering;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class SortOrderingImpl implements SortOrdering {
	private String _key = null;
	private int _type = 0;
	private Order _order = null;

	/**
	 * 
	 */
	public SortOrderingImpl(String in_key, int in_type) {
		switch(in_type) {
			case SortOrdering.ASC : _order = Order.asc(in_key);break;
			case SortOrdering.DESC : _order = Order.asc(in_key);break;
		}
		_key = in_key;
		_type = in_type;
	}

	/**
	 * @see fr.improve.xdi.mapping.SortOrdering#getKey()
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * @see fr.improve.xdi.mapping.SortOrdering#getType()
	 */
	public int getType() {
		return _type;
	}

	/**
	 * @see fr.improve.xdi.mapping.SortOrdering#getOrder()
	 */
	public Object getOrder() {
		return _order;
	}

}
