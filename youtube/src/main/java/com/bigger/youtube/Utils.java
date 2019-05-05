package com.bigger.youtube;

public class Utils {

    public static String printStackTrace() {
        return printStackTrace(null, null);
    }

    public static String printStackTrace(StackTraceElement[] stackTrace) {
        return printStackTrace(stackTrace, null);
    }

    public static String printStackTrace(String tag) {
        return printStackTrace(null, tag);
    }

    public static String printStackTrace(StackTraceElement[] stackTrace, String tag) {
        if (stackTrace == null) {
            stackTrace = Thread.currentThread().getStackTrace();
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(tag == null? "": tag).append("---------------------------------------").append("\r");
        for (StackTraceElement element : stackTrace) {
            buffer.append("    ").append(element.getClassName()).append(".").append(element.getMethodName()).append("\r");
        }
        buffer.append("---------------------------------------").append("\r");
        return buffer.toString();
    }
}
