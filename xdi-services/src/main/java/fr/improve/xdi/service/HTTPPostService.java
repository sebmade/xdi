package fr.improve.xdi.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HTTPPostService implements Service {

    public Document callService(Hashtable in_parameters) throws ServiceException {
        String content = (String) in_parameters.get(Service.HTTP_CONTENT);
        if (content != null) {
            // Get target URL 
            String strURL = (String) in_parameters.get(Service.HTTP_URL);

            // Prepare HTTP post
            PostMethod post = new PostMethod(strURL);
            // Request content will be retrieved directly
            // from the input stream
            String encoding = (String) in_parameters.get(Service.ENCODING);
            if (encoding == null) {
                encoding = "UTF-8";
            }
            String contentType = (String) in_parameters.get(Service.HTTP_CONTENT_TYPE);
            if (contentType == null) {
                contentType = "text/xml";
            }
            try {
                RequestEntity entity = new StringRequestEntity(content, contentType, encoding);
                post.setRequestEntity(entity);

                // Get HTTP client
                HttpClient httpclient = new HttpClient();
                String l_proxyHost = (String) in_parameters.get(Service.HTTP_PROXY_HOST);
                String l_proxyPort = (String) in_parameters.get(Service.HTTP_PROXY_PORT);
                if ((l_proxyHost != null) && (l_proxyPort != null)) {
                    String l_proxyLogin = (String) in_parameters.get(Service.HTTP_PROXY_LOGIN);
                    String l_proxyPass = (String) in_parameters.get(Service.HTTP_PROXY_PWD);
                    httpclient.getHostConfiguration().setProxy(l_proxyHost,
                                                               Integer.valueOf(l_proxyPort).intValue());
                    if ((l_proxyLogin != null) && (l_proxyPass != null)) {
                        httpclient.getState().setProxyCredentials(new AuthScope(l_proxyHost,
                                                                                Integer.valueOf(l_proxyPort).intValue()),
                                                                  new UsernamePasswordCredentials(l_proxyLogin,
                                                                                                  l_proxyPass));
                    }
                }
                // Execute request
                int statusCode = httpclient.executeMethod(post);
                String result = null;
                if (statusCode == 200) {
                    result = post.getResponseBodyAsString();
                } else {
                    throw new ServiceException("response code : "+statusCode); 
                }
                if (post.getResponseHeader("Content-Type") != null && post.getResponseHeader("Content-Type").getValue().indexOf("text/xml") != -1 && (result != null && result.trim().length() > 0 && result.startsWith("<?xml"))) {
                    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(result)));
                } else {
                    return null;
                }
                
            } catch (UnsupportedEncodingException e) {
                throw new ServiceException(e);
            } catch (HttpException e) {
                throw new ServiceException(e);
            } catch (IOException e) {
                throw new ServiceException(e);
            } catch (SAXException e) {
                throw new ServiceException(e);
            } catch (ParserConfigurationException e) {
                throw new ServiceException(e);
            } finally {
                // Release current connection to the connection pool once you are done
                post.releaseConnection();
            }
        }
        return null;
    }

}
