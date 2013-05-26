package fr.improve.xdi.mapping;

import java.io.Serializable;

public class Attribute implements Serializable {
	protected String key;
	protected Class type;

    public Attribute(String in_name, Class in_type) {
        key = null;
        type = null;
        key = in_name;
        type = in_type;
    }

    public String getKey() {
        return key;
    }

    public Class getType() {
        return type;
    }

    public void setKey(String string) {
        key = string;
    }

    public void setType(Class class1) {
        type = class1;
    }

}
