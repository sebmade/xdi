package fr.improve.xdi.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * HTTP service
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class HTTPSenderService implements Service {
    static Log log = LogFactory.getLog(HTTPSenderService.class);
	private static DocumentBuilderFactory _factory;
	private static DocumentBuilder _builder;

	static {
		try {
			_factory = DocumentBuilderFactory.newInstance();
			_builder = _factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.fatal("INTEGRATION ERREUR : impossible de charger le parser DOM", e);
		}
	}

    /* 
     * @see fr.improve.xdi.service.Service#callService(java.util.Hashtable)
     */
    public Document callService(Hashtable in_parameters) throws ServiceException {
		PostMethod l_post = new PostMethod((String) in_parameters.get(Service.HTTP_URL));
		Object l_content = in_parameters.get(Service.HTTP_CONTENT);
		if (l_content instanceof byte[]) {
			if (((byte[])l_content).length < Integer.MAX_VALUE) {
				l_post.setRequestContentLength(((byte[])l_content).length);
			} else {
				l_post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
			}
		    l_content = new ByteArrayInputStream((byte[])l_content); 
		} else {
			l_post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
		}
		l_post.setRequestBody((InputStream)l_content);
		String l_contentType = (String) in_parameters.get(Service.HTTP_CONTENT_TYPE);
		if (l_contentType != null) {
		    l_post.setRequestHeader("Content-type", l_contentType+"; charset=ISO-8859-1");
		}
		String l_filename = (String) in_parameters.get(Service.HTTP_FILENAME);
		if (l_filename != null) {
		    l_post.setRequestHeader("Content-Disposition", "attachment; filename="+l_filename);
		}

		HttpClient l_client = new HttpClient();
        String l_proxyHost = (String)in_parameters.get(Service.HTTP_PROXY_HOST);
        String l_proxyPort = (String)in_parameters.get(Service.HTTP_PROXY_PORT);
		if ((l_proxyHost != null) && (l_proxyPort != null)) {
	        String l_proxyLogin = (String)in_parameters.get(Service.HTTP_PROXY_LOGIN);
	        String l_proxyPass = (String)in_parameters.get(Service.HTTP_PROXY_PWD);
	        l_client.getHostConfiguration().setProxy(l_proxyHost, Integer.valueOf(l_proxyPort).intValue());
			if ((l_proxyLogin != null) && (l_proxyPass != null)) {
			    l_client.getState().setProxyCredentials(null, l_proxyHost, new UsernamePasswordCredentials(l_proxyLogin, l_proxyPass));
			}
		}

		try {
            log.debug("Connect and post ...");
            int l_statusCode = l_client.executeMethod(l_post);
            String l_result = null;
            if (l_statusCode == 200) {
            	l_result = l_post.getResponseBodyAsString();
            } else {
            	throw new ServiceException("response code : "+l_statusCode); 
            }
            l_post.releaseConnection();
            log.debug("end.");

            if (l_post.getResponseHeader("Content-Type") != null && l_post.getResponseHeader("Content-Type").getValue().indexOf("text/xml") != -1 && (l_result != null && l_result.trim().length() > 0 && l_result.startsWith("<?xml"))) {
                return _builder.parse(new InputSource(new StringReader(l_result)));
            } else {
                return null;
            }
        } catch (HttpException e) {
            throw new ServiceException(e);
        } catch (IOException e) {
            throw new ServiceException(e);
        } catch (SAXException e) {
            throw new ServiceException(e);
        }
    }

}
