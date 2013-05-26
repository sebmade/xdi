package fr.improve.xdi.mapping;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class StringUtils implements Serializable {
    public StringUtils() {
    }

    public static String toKeyString(String s) {
        StringTokenizer l_tkz = new StringTokenizer(s, "_");
        StringBuffer l_str = new StringBuffer(l_tkz.nextToken().toLowerCase());

        for (; l_tkz.hasMoreElements(); l_str.append(capitalizedString(l_tkz.nextToken().toLowerCase()))) {
            ;
        }

        return l_str.toString();
    }

    public static String classString(String s) {
        StringTokenizer l_tkz = new StringTokenizer(s, "_");
        StringBuffer l_str = new StringBuffer();

        for (; l_tkz.hasMoreElements(); l_str.append(capitalizedString(l_tkz.nextToken().toLowerCase()))) {
            ;
        }

        return l_str.toString();
    }

    public static String tableNameString(String s) {
        StringBuffer l_result = new StringBuffer(s);

        for (int i = 1; i < s.length(); i++) {
            if (Character.isUpperCase(l_result.charAt(i))) {
                l_result.insert(i++, '_');
            }
        }

        return l_result.toString().toUpperCase();
    }

    public static String toManyKeyString(String s) {
        StringTokenizer l_tkz = new StringTokenizer(s, "_");
        StringBuffer l_str = new StringBuffer(l_tkz.nextToken().toLowerCase());

        for (; l_tkz.hasMoreElements(); l_str.append(capitalizedString(l_tkz.nextToken().toLowerCase()))) {
            ;
        }

        return l_str.toString() + "s";
    }

    public static String capitalizedString(String s) {
        if (s != null) {
            int i = s.length();

            if (i > 0) {
                char c = s.charAt(0);

                if (!Character.isUpperCase(c)) {
                    StringBuffer stringbuffer = new StringBuffer(s.length());
                    stringbuffer.append(Character.toUpperCase(c));

                    if (i > 1) {
                        stringbuffer.append(s.substring(1));
                    }

                    s = new String(stringbuffer);
                }
            }
        }

        return s;
    }

    public static String uncapitalizedString(String s) {
        if (s != null) {
            int i = s.length();

            if (i > 0) {
                char c = s.charAt(0);

                if (Character.isUpperCase(c)) {
                    StringBuffer stringbuffer = new StringBuffer(s.length());
                    stringbuffer.append(Character.toLowerCase(c));

                    if (i > 1) {
                        stringbuffer.append(s.substring(1));
                    }

                    s = new String(stringbuffer);
                }
            }
        }

        return s;
    }

    public static String setMethod(String in_key) {
        return "set" + capitalizedString(in_key);
    }

    public static String getMethod(String in_key) {
        return "get" + capitalizedString(in_key);
    }

    public static String isMethod(String in_key) {
        return "is" + capitalizedString(in_key);
    }

    public static String removeMethod(String in_key) {
        return "remove" + capitalizedString(in_key);
    }

    public static Vector vectorFromCommaSeparatedString(String in_str) {
        Vector l_array = new Vector();

        for (StringTokenizer l_st = new StringTokenizer(in_str, ","); l_st.hasMoreTokens(); l_array.add(l_st.nextToken())) {
            ;
        }

        return l_array;
    }
}
