package fr.improve.xdi.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.SerializerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import fr.improve.xdi.resources.Messages;

/**
 * HTTP GET service
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 */
public class HTTPService implements Service {
    static Log log = LogFactory.getLog(HTTPService.class);

    /**
     *  
     */
    public HTTPService() {
        super();
    }

    /*
     * @see fr.improve.xdi.service.Service#callService(java.util.Hashtable)
     */
    public Document callService(Hashtable in_parameters) throws ServiceException {
        // Create an instance of HttpClient.
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

        // Create a method instance.
        String l_url =(String) in_parameters.get(Service.HTTP_URL);
        log.debug("URL : "+l_url);
        HttpMethod l_method = null;
        if ("POST".equals(in_parameters.get(Service.HTTP_METHOD))) {
            l_method = new PostMethod(l_url);
        } else {
            l_method = new GetMethod(l_url);
        }
        String l_query = (String)in_parameters.get(Service.HTTP_QUERY);
        if (l_query != null) {
            l_method.setQueryString(l_query);
            log.debug("Query String : "+l_query);
        }

        // Execute the method.
        int l_statusCode = -1;
        int l_attempt = 0;
        
        // We will retry up to 3 times.
        while (l_statusCode == -1 && l_attempt < 3) {
            l_attempt++;
            try {
                // execute the method.
                l_statusCode = l_client.executeMethod(l_method);
            } catch (HttpRecoverableException e) {
                log.error(Messages.getMessage("httpRecoverException00", l_url), e);
            } catch (IOException e) {
                log.error(Messages.getMessage("httpError00", l_url), e);
            }
        }
        
        // Read the response body.
        Document l_result = null;
        if (l_statusCode != -1) {
            try {
                l_result = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(l_method.getResponseBodyAsStream()));
                if (log.isDebugEnabled()) {
                    StringWriter l_writer = new StringWriter();
                    SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(l_writer, new OutputFormat(Method.XML, "iso-8859-1", true)).asDOMSerializer().serialize(l_result);
                    log.debug("XML result :");
                    log.debug(l_writer.toString());
                }
            } catch (Exception e) {
                throw new ServiceException(Messages.getMessage("httpDocumentResult00"), e);
            }
        } else {
            throw new ServiceException(Messages.getMessage("httpError01", l_url));
        }

        // Release the connection.
        l_method.releaseConnection();

        return l_result;
    }

}