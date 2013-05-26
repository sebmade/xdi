package fr.improve.xdi.converter;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import org.w3c.dom.Document;

import fr.improve.xdi.Integrator;

public interface Converter extends Serializable {
    public static final int CSV = 0;
    public static final int FIELD_LENGTH = 1;
    public static final int HPRIM = 2;
    public static final int XML = 3;
    public static final int NX = 5;

    public void setConfig(URL in_filePath) throws IOException;
    public void setConfig(Properties in_properties) throws IOException;

    public Properties getConfig();

    public void convertToXML(Reader in_reader, Writer in_writer) throws ConverterException;

    public void convertAndIntegrate(Integrator in_integrator, Reader in_file, Reader in_xsl, boolean in_withSchemaValidation) throws ConverterException;

    public void convertFromXML(Reader in_reader, Writer in_writer) throws ConverterException;

    public void convertFromXML(Document in_document, Writer in_writer) throws ConverterException;
}
