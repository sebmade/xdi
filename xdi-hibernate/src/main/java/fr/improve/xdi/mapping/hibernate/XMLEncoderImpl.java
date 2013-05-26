package fr.improve.xdi.mapping.hibernate;

import java.util.Collection;

import fr.improve.xdi.DefaultXMLEncoder;
import fr.improve.xdi.mapping.EnterpriseObject;

/**
 * XMLEncoder implementation for Hibernate
 * Used to convert Hibernate objects to EnterpriseObject
 * 
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class XMLEncoderImpl extends DefaultXMLEncoder {

	/**
	 *
	 */
	public XMLEncoderImpl() {
		super();
	}

	/**
	 * @see fr.improve.edi.integrator.XMLEncoder#encodeObjects(java.lang.Object)
	 */
	public void encodeObjects(Object in_records) {
		if (in_records instanceof Collection) {
			super.encodeObjects((Collection)in_records);
		}
	}

	public void encodeObjects(Object in_records, String in_rootName) {
		if (in_records instanceof Collection) {
			super.encodeObjects((Collection)in_records, in_rootName);
		}
	}
	
	/**
	 * @see fr.improve.edi.integrator.XMLEncoder#encodeObject(java.lang.Object)
	 */
	public void encodeObject(Object in_record) {
		if (in_record instanceof EnterpriseObject) {
			super.encodeObject((EnterpriseObject)in_record);
		}
	}

}
