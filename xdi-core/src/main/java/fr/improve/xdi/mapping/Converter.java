package fr.improve.xdi.mapping;

import java.io.Serializable;

public interface Converter extends Serializable {
    public Object convert(Object obj);
}
