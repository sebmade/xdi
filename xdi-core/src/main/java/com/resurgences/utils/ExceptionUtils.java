package com.resurgences.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class ExceptionUtils {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExceptionUtils.class);

    public static RuntimeException wrap(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }

     public static boolean isOrContainsCause(Throwable exception, Class<? extends Throwable> ... otherExceptionsLookedFor) {
         if (exception == null){
             return false;
         }
         if (otherExceptionsLookedFor == null || otherExceptionsLookedFor.length == 0) {
             return false;
         }
         Set<Class<? extends Throwable>> effectiveExceptionsLookedFor = new HashSet<Class<? extends Throwable>>(Arrays.asList(otherExceptionsLookedFor));
         if (effectiveExceptionsLookedFor.contains(exception.getClass())) {
             return true;
         }
         List<? extends Throwable> throwableList = org.apache.commons.lang.exception.ExceptionUtils.getThrowableList(exception);

         for (Throwable throwable : throwableList) {
             for (Class exceptionClass : effectiveExceptionsLookedFor) {
                 if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                     return true;
                 }
             }
         }
         return false;
     }



    public static String strackTraceToString(Throwable t) {
        return org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(t);
    }

    public static void rethrowIfNeeded(Throwable e) throws RuntimeException {
        if (e != null && isOrContainsCause(e, OutOfMemoryError.class)) {
            log.error("OutOfMemory detected and rethrown, please make sure it made its way up", e);
            throw wrap(e);
        }
    }

    public static String getFullStackTrace(Throwable targetException) {
        return org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(targetException);
    }

    public static String getAllStackTraces(boolean html) {
        StringBuffer l_response = new StringBuffer();
        SortedMap<Thread, StackTraceElement[]> l_stacks = new TreeMap(Thread.getAllStackTraces());

        for (Entry<Thread, StackTraceElement[]> entry : l_stacks.entrySet()) {
            Thread l_thread = entry.getKey();
            StackTraceElement[] trace = entry.getValue();

            if (html)
                l_response.append("<b>"+l_thread.toString()+"</b><br>");
            else
                l_response.append(l_thread.toString()+"\n");

            for (int j=0; j < trace.length; j++) {
                if (html)
                    l_response.append("&nbsp;&nbsp;&nbsp;&nbsp;" + trace[j]+"<br>");
                else
                    l_response.append("\tat " + trace[j]+"\n");
            }
        }

        return l_response.toString();
    }

}
