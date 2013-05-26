package fr.improve.xdi.encutils;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class XMLEncodingConfigDTDResolver implements EntityResolver {
    
    @Override
	public InputSource resolveEntity(String publicId, String systemId)
		throws SAXException, IOException {
		return new InputSource(getClass().getResourceAsStream("XMLEncodingConfig.dtd"));
	}

}
