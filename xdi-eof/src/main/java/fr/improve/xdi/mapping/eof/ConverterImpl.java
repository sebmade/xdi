package fr.improve.xdi.mapping.eof;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import fr.improve.xdi.mapping.Converter;

/**
 * @deprecated
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class ConverterImpl implements Converter {
    public ConverterImpl() {
    }

    public Object convert(Object in_object) {
        if (in_object instanceof NSArray) {
            return ((NSArray) in_object).vector();
        }

        if (in_object instanceof EOEnterpriseObject) {
            return new EnterpriseObjectImpl((EOEnterpriseObject) in_object);
        } else {
            return null;
        }
    }
}
