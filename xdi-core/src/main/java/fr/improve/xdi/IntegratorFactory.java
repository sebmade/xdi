package fr.improve.xdi;

import com.resurgences.utils.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.improve.xdi.mapping.EnterpriseContext;
import fr.improve.xdi.resources.Messages;

/**
 * Import / Export Factory<br>
 *
 * Before using XDI you must define the mapping tool EOF or Hibernate :
 * 		<ul><li>IntegratorFactory.setMapping([IntegratorFactory.EOF|IntegratorFactory.HIBERNATE]);</li></ul>
 *
 * Implementation must be define for class :
 * 		<ul>
 * 			<li>fr.improve.xdi.mapping.EnterpriseContext</li>
 * 			<li>fr.improve.xdi.XMLEncoder</li>
 * 		</ul>
 * Default implementation are define in :
 * 		<ul>
 * 			<li>EOF : eof.properties (package fr.improve.xdi.mapping.eof)</li>
 * 			<li>Hibernate : hibernate.properties (package fr.improve.xdi.mapping.hibernate)</li>
 * 		</ul>
 * You can define your own implementations using :
 * 		<ul><li>IntegratorFactory.setIntegratorProperties(<StreamOfPropertiesFile>);</li></ul>
 *
 * Mapping properties must be set for Hibernate :
 * 		<ul>
 * 			<li>IntegratorFactory.setMappingProperties(<URLOfHBMConfigurationFile>);</li>
 *		</ul>
 *		<ul>
 * 		<li>
 * 			The Hibernate configuration file must contain followings elements :
 * 			<ul>
 * 				<li>
 * 						the package containing the enterprise layer of the mapping objects<br>
 * 						&lt;property name="package.enterprise"&gt;my.package.to.enterprise&lt;/property&gt;
 * 				</li>
 * 				<li>
 * 					the package containing the mapping layer of the mapping objects<br>
 *					&lt;property name="package.mapping"&gt;my.package.to.mapping&lt;/property&gt;
 *				</li>
 *			</ul>
 *		</li>
 *		</ul>
 * Use HibernateSynchroniser Eclipse plugin for an easy Hibernate integration<br>
 *
 * For calling a new import controller :
 * 	<ul><li>IntegratorFactory.getInstance().newIntegrator();</li></ul>
 *
 * For calling a new export controller :
 * 	<ul><li>IntegratorFactory.getInstance.newEncoder();</li></ul>
 *
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class IntegratorFactory {
    static Log log = LogFactory.getLog(Integrator.class);

    private static final ThreadLocal threadLocal = new ThreadLocal();

    //private static IntegratorFactory _factory;
    private static Properties _properties = new Properties();

    private static URL _mappingPropertiesURL = null;

    private Class _enterpriseContextImpl;

    private Class _xmlEncoderImpl;

    private Class _xmlDecoderImpl;

    private Class _integrateHandlerImpl;

    private EnterpriseContext _enterpriseContext;

    public static final int EOF = 0;

    public static final int HIBERNATE = 1;

    /**
     * Set the implementation properties file
     * @param in_stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void setIntegratorProperties(InputStream in_stream) throws IOException,
            ClassNotFoundException {
        _properties.load(in_stream);
        threadLocal.set(new IntegratorFactory());
    }

    /**
     * Set the hibernate mapping XML file
     * @param in_url
     * @throws IOException
     */
    public static void setMappingProperties(URL in_url) throws IOException {
        _mappingPropertiesURL = in_url;
    }

    /**
     * Set the mapping tool
     * @param in_type : EOF for EOF, HIBERNATE for Hibernate
     */
    public static void setMapping(int in_type) {
        try {
            switch (in_type) {
            case EOF:
                _properties.load(IntegratorFactory.class.getResourceAsStream("eof.properties"));

                break;
            case HIBERNATE:
                _properties.load(IntegratorFactory.class.getResourceAsStream("hibernate.properties"));
                break;
            }
        } catch (IOException e) {
            log.warn("Exception occurred", e);
        }
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException : if an implementation class is not found
     */
    public IntegratorFactory() throws IOException, ClassNotFoundException {
        _enterpriseContextImpl = _loadImplClass(EnterpriseContext.class.getName());

        if (_enterpriseContextImpl == null) {
            throw new ClassNotFoundException(Messages.getMessage("loadImplClass00",
                                                                 EnterpriseContext.class.getName()));
        }

        _xmlEncoderImpl = _loadImplClass(XMLEncoder.class.getName());

        if (_xmlEncoderImpl == null) {
            throw new ClassNotFoundException(Messages.getMessage("loadImplClass00",
                                                                 XMLEncoder.class.getName()));
        }

        _xmlDecoderImpl = _loadImplClass(XMLDecoder.class.getName());

        if (_xmlDecoderImpl == null) {
            _xmlDecoderImpl = DefaultXMLDecoder.class;
        }

        _integrateHandlerImpl = _loadImplClass(IntegrateHandler.class.getName());

        if (_integrateHandlerImpl == null) {
            _integrateHandlerImpl = DefaultIntegrateHandler.class;
        }
    }

    /**
     * Singleton pattern
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static synchronized IntegratorFactory getInstance() throws IOException,
            ClassNotFoundException {
        IntegratorFactory intf = (IntegratorFactory) threadLocal.get();
        if (intf == null) {
            intf = new IntegratorFactory();
            threadLocal.set(intf);
        }

        return intf;
    }

    private Class _loadImplClass(String in_key) throws ClassNotFoundException {
        String l_className = _properties.getProperty(in_key);
        Class l_class = null;

        if (l_className != null) {
            /*Runtime.getRuntime().traceInstructions(true);
               Runtime.getRuntime().traceMethodCalls(true);
               Package[] l_list = Package.getPackages();
               for (int i=0; i<l_list.length; i++) {
                       log.debug(l_list[i]);
               }*/

            //try {
            l_class = Class.forName(l_className);

            /*} catch (ClassNotFoundException e) {
               ClassLoader l_loader = IntegratorFactory.class.getClassLoader();
               log.debug("loader = "+l_loader);
               if (l_loader != null) {
                       log.debug("resource : "+l_loader.getResource("."));
                       try {
                               l_class = l_loader.loadClass(l_className);
                       } catch (ClassNotFoundException e1) {
                               l_loader = l_loader.getParent();
                               log.debug("parent loader = "+l_loader);
                               if (l_loader != null) {
                                       log.debug("resource : "+l_loader.getResource("."));
                                       try {
                                               l_class = l_loader.loadClass(l_className);
                                       } catch (ClassNotFoundException e2) {
                                               throw e2;
                                       }
                               } else {
                                       l_loader = ClassLoader.getSystemClassLoader();
                                       log.debug("system loader = "+l_loader);
                                       if (l_loader != null) {
                                               log.debug("resource : "+l_loader.getResource("."));
                                               try {
                                                       l_class = l_loader.loadClass(l_className);
                                               } catch (ClassNotFoundException e2) {
                                                       throw e2;
                                               }
                                       }
                               }
                       }
               } else throw e;
               }
               Runtime.getRuntime().traceInstructions(false);
               Runtime.getRuntime().traceMethodCalls(false);*/
        }

        return l_class;
    }

    /**
     * Get the EnterpriseContext implementation
     * @see fr.improve.mapping.EnterpriseContext
     * @return EnterpriseContext implementation instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public EnterpriseContext getEnterpriseContext() throws InstantiationException,
            IllegalAccessException {
        if (_enterpriseContext == null) {
            try {
                Constructor l_constructor = _enterpriseContextImpl.getConstructor(new Class[] { URL.class });
                _enterpriseContext = (EnterpriseContext) l_constructor.newInstance(new Object[] { _mappingPropertiesURL });
            } catch (InvocationTargetException e) {
                log.error(e.getTargetException(),e);
                throw new InstantiationException(e.getTargetException().getMessage());
            } catch (Exception e) {
                ExceptionUtils.rethrowIfNeeded(e);
                _enterpriseContext = (EnterpriseContext) _enterpriseContextImpl.newInstance();
            }
        }

        return _enterpriseContext;
    }

    public void setEnterpriseContext(EnterpriseContext in_ec) {
        _enterpriseContext = in_ec;
    }

    /**
     * Get the import controller
     * @return import controller
     * @throws ParserConfigurationException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public synchronized Integrator newIntegrator() throws ParserConfigurationException,
            InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        return new Integrator(newDecoder());
    }

    /**
     * Get the export controller
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public synchronized XMLEncoder newEncoder() throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        XMLEncoder l_encoder = (XMLEncoder) _xmlEncoderImpl.newInstance();

        //l_encoder.setConverter((Converter) Class.forName("fr.improve.edi.integrator." + l_context + ".ConverterImpl").newInstance());
        return l_encoder;
    }

    /**
     * Get the XML decoder used by the import controller
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public synchronized XMLDecoder newDecoder() throws InstantiationException,
            IllegalAccessException {
        XMLDecoder l_decoder = null;

        try {
            Constructor l_constructor = _xmlDecoderImpl.getConstructor(new Class[] { EnterpriseContext.class, Class.class });
            l_decoder = (XMLDecoder) l_constructor.newInstance(new Object[] { getEnterpriseContext(), _integrateHandlerImpl });
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException(),e);
            throw new InstantiationException(e.getTargetException().getMessage());
        } catch (Exception e) {
            ExceptionUtils.rethrowIfNeeded(e);
            l_decoder = (XMLDecoder) _xmlDecoderImpl.newInstance();
        }

        return l_decoder;
    }
}
