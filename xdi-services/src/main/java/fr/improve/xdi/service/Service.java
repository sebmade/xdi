package fr.improve.xdi.service;

import java.io.Serializable;
import java.util.Hashtable;

import org.w3c.dom.Document;

/**
 * Exchange service interface
 *  
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface Service extends Serializable {
    public final static short HTTP_XML = 0;

    public final static short WEB_SERVICES = 1;

    public final static short HTTP = 2;

    public static final short HTTP_SENDER = 3;

    public static final short HTTP_POST = 5;

    public static final int FILEBOX = 4;

    public final static String HTTP_URL = "edi.service.http.url";

    public final static String HTTP_METHOD = "edi.service.http.method";

    public final static String HTTP_PROXY_HOST = "edi.service.http.proxy.host";

    public final static String HTTP_PROXY_PORT = "edi.service.http.proxy.port";

    public static final String FILEBOX_PATH = "edi.service.filebox.path";

    public static final String FILEBOX_FILENAME = "edi.service.filebox.filename";

    public final static String HTTP_PROXY_LOGIN = "edi.service.http.proxy.login";

    public final static String HTTP_PROXY_PWD = "edi.service.http.proxy.password";

    public final static String HTTP_XML_DOCUMENT = "edi.service.httpxml.document";

    public static final String FILEBOX_CONTENT = "edi.service.filebox.contennt";

    public final static String HTTP_XML_ATTRIBUTES = "edi.service.httpxml.attributes";

    public static final String WEB_SERVICE_MSG_TYPE = "MSG";

    public static final String HTTP_CONTENT = "edi.service.http.content";

    public static final String WEB_SERVICE_TYPE = "edi.service.web.type";

    public static final String WEB_SERVICE_DOCUMENT = "edi.service.web.document";

    public static final String WEB_SERVICE_TARGET_ENDPOINT = "edi.service.web.target";

    public static final String WEB_SERVICE_ATTACHMENTS = "edi.service.web.attachments";

    public static final String WEB_SERVICE_NSPREFIX = "svc";

    public static final String WEB_SERVICE_NAME = "edi.service.web.name";

    public static final String HTTP_CONTENT_TYPE = "edi.service.http.contenttype";

    public static final String WEB_SERVICE_NAMESPACE = "edi.service.web.namespace";

    public static final String HTTP_QUERY = "edi.service.http.query";

    public static final String HTTP_FILENAME = "edi.service.http.filename";

    public static final String ENCODING = "enconding";


    public Document callService(Hashtable in_parameters) throws ServiceException;
}