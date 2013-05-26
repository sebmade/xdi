package fr.improve.xdi.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Converter factory, 3 implementations are used :
 * 		- CSV : comma separated value text format
 * 		- FIELD_LENGTH : fixed field length text format
 * 		- HPRIM : specific medical format
 *
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class ConverterFactory {
    static Log log = LogFactory.getLog(ConverterFactory.class);
    private ConverterFactory() {
    }

    public static ConverterFactory getInstance() {
        if (_factory == null) {
            _factory = new ConverterFactory();
        }

        return _factory;
    }

    public Converter getConverter(int in_type) {
        try {
            switch (in_type) {
                case Converter.CSV:
                    return (Converter) Class.forName("fr.improve.resedim.converter.csv.CSVConverter").newInstance();
                case Converter.FIELD_LENGTH:
                    return (Converter) Class.forName("fr.improve.resedim.converter.fieldlength.FieldLengthConverter").newInstance();
                case Converter.HPRIM:
                    return (Converter) Class.forName("fr.improve.resedim.converter.hprim.HPrimConverter").newInstance();
                case Converter.NX:
                    return (Converter) Class.forName("fr.improve.resedim.converter.nx.NxConverter").newInstance();
            }
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error("Conversion type not found, is xdi-converters.jar in your CLASSPATH", e);
        }

        return null;
    }

    private static ConverterFactory _factory;
}
