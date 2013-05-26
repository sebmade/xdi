package fr.improve.xdi;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.webobjects.foundation.NSTimestamp;

public class DefaultXMLDecoderTest {

    @Test
    public void decodeDateShouldAlwaysReturnGivenClassType() throws Exception {
        DefaultXMLDecoder decoder = new DefaultXMLDecoder(null, null);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = document.createElement("foo");
        Text textNode = document.createTextNode("dontCare");
        textNode.setNodeValue(DefaultXMLDecoder.NOW_KEY);
        element.appendChild(textNode);
        Object result = decoder._decodeDate(element, NSTimestamp.class);
        Assert.assertTrue(result instanceof NSTimestamp);
    }
}
