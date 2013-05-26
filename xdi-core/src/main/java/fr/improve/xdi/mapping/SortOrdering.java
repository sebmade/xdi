package fr.improve.xdi.mapping;

import java.io.Serializable;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface SortOrdering extends Serializable {
	public static final int ASC = 0;
	public static final int DESC = 1;
	
	public String getKey();
	public int getType();
	public Object getOrder();

}
