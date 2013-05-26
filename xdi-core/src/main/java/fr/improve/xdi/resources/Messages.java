package fr.improve.xdi.resources;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * XDI messages resources
 *
 * @author Sebastien Letelie <s.letelie@improve.fr>
 *
 */
public class Messages implements Serializable {
    static Log log = LogFactory.getLog(Messages.class);

    private static ResourceBundle _resources = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".resource");

    public static String getMessage(String key) throws MissingResourceException {
        return _resources.getString(key);
    }

    public static String getMessage(String key, String arg0) throws MissingResourceException {
        return getMessage(key, new String[] { arg0 });
    }

    public static String getMessage(String key, String arg0, String arg1)
            throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1 });
    }

    public static String getMessage(String key, String arg0, String arg1, String arg2)
            throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1, arg2 });
    }

    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3)
            throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1, arg2, arg3 });
    }

    public static String getMessage(String key,
                                    String arg0,
                                    String arg1,
                                    String arg2,
                                    String arg3,
                                    String arg4) throws MissingResourceException {
        return getMessage(key, new String[] { arg0, arg1, arg2, arg3, arg4 });
    }

    public static String getMessage(String in_key, String[] in_args) {
        String l_msg = null;

        if (_resources != null) {
            l_msg = _resources.getString(in_key);
        }

        if (l_msg == null) {
            throw new MissingResourceException("Cannot find resource key \"" + in_key + "\"",
                                               "messages",
                                               in_key);
        } else {
            return MessageFormat.format(l_msg, in_args);
        }
    }

}