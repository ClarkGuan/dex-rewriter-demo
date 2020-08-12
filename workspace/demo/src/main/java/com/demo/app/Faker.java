package com.demo.app;

public class Faker {

    public static class Thread extends java.lang.Thread {
        public Thread() {
            setName(getStackTraceElement("new Thread()", 2));
        }
    }

    public static String getStackTraceElement(String prefix, int i) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return String.format("%s defined in %s:%d", prefix, stackTrace[i].getFileName(), stackTrace[i].getLineNumber());
    }
}
