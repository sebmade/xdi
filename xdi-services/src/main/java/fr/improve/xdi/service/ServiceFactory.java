package fr.improve.xdi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Exchange service factory
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class ServiceFactory {
    static Log log = LogFactory.getLog(ServiceFactory.class.getName());

    private static ServiceFactory _factory;

    public static synchronized ServiceFactory getInstance() {
        if (_factory == null) {
            _factory = new ServiceFactory();
        }

        return _factory;
    }

    public Service getService(int in_type) {
        switch (in_type) {
        case Service.HTTP_POST: {
            return new HTTPPostService();
        }
        case Service.HTTP_XML: {
            return new HTTPXMLService();
        }
        case Service.WEB_SERVICES: {
            return new WebService();
        }
        case Service.HTTP: {
            return new HTTPService();
        }
        case Service.HTTP_SENDER: {
            return new HTTPSenderService();
        }
        case Service.FILEBOX: {
            return new FileBoxService();
        }
        }
        return null;
    }

}
