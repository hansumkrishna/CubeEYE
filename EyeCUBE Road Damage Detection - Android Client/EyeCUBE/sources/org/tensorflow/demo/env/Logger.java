package org.tensorflow.demo.env;

import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public final class Logger {
    private static final int DEFAULT_MIN_LOG_LEVEL = 3;
    private static final String DEFAULT_TAG = "tensorflow";
    private static final Set<String> IGNORED_CLASS_NAMES = new HashSet(3);
    private final String messagePrefix;
    private int minLogLevel;
    private final String tag;

    static {
        IGNORED_CLASS_NAMES.add("dalvik.system.VMStack");
        IGNORED_CLASS_NAMES.add("java.lang.Thread");
        IGNORED_CLASS_NAMES.add(Logger.class.getCanonicalName());
    }

    public Logger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public Logger(String messagePrefix2) {
        this(DEFAULT_TAG, messagePrefix2);
    }

    public Logger(String tag2, String messagePrefix2) {
        String prefix;
        this.minLogLevel = 3;
        this.tag = tag2;
        if (messagePrefix2 == null) {
            prefix = getCallerSimpleName();
        } else {
            prefix = messagePrefix2;
        }
        this.messagePrefix = prefix.length() > 0 ? prefix + ": " : prefix;
    }

    public Logger() {
        this(DEFAULT_TAG, (String) null);
    }

    public Logger(int minLogLevel2) {
        this(DEFAULT_TAG, (String) null);
        this.minLogLevel = minLogLevel2;
    }

    public void setMinLogLevel(int minLogLevel2) {
        this.minLogLevel = minLogLevel2;
    }

    public boolean isLoggable(int logLevel) {
        return logLevel >= this.minLogLevel || Log.isLoggable(this.tag, logLevel);
    }

    private static String getCallerSimpleName() {
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            String className = elem.getClassName();
            if (!IGNORED_CLASS_NAMES.contains(className)) {
                String[] classParts = className.split("\\.");
                return classParts[classParts.length - 1];
            }
        }
        return Logger.class.getSimpleName();
    }

    private String toMessage(String format, Object... args) {
        StringBuilder append = new StringBuilder().append(this.messagePrefix);
        if (args.length > 0) {
            format = String.format(format, args);
        }
        return append.append(format).toString();
    }

    /* renamed from: v */
    public void mo6298v(String format, Object... args) {
        if (isLoggable(2)) {
            Log.v(this.tag, toMessage(format, args));
        }
    }

    /* renamed from: v */
    public void mo6299v(Throwable t, String format, Object... args) {
        if (isLoggable(2)) {
            Log.v(this.tag, toMessage(format, args), t);
        }
    }

    /* renamed from: d */
    public void mo6290d(String format, Object... args) {
        if (isLoggable(3)) {
            Log.d(this.tag, toMessage(format, args));
        }
    }

    /* renamed from: d */
    public void mo6291d(Throwable t, String format, Object... args) {
        if (isLoggable(3)) {
            Log.d(this.tag, toMessage(format, args), t);
        }
    }

    /* renamed from: i */
    public void mo6294i(String format, Object... args) {
        if (isLoggable(4)) {
            Log.i(this.tag, toMessage(format, args));
        }
    }

    /* renamed from: i */
    public void mo6295i(Throwable t, String format, Object... args) {
        if (isLoggable(4)) {
            Log.i(this.tag, toMessage(format, args), t);
        }
    }

    /* renamed from: w */
    public void mo6300w(String format, Object... args) {
        if (isLoggable(5)) {
            Log.w(this.tag, toMessage(format, args));
        }
    }

    /* renamed from: w */
    public void mo6301w(Throwable t, String format, Object... args) {
        if (isLoggable(5)) {
            Log.w(this.tag, toMessage(format, args), t);
        }
    }

    /* renamed from: e */
    public void mo6292e(String format, Object... args) {
        if (isLoggable(6)) {
            Log.e(this.tag, toMessage(format, args));
        }
    }

    /* renamed from: e */
    public void mo6293e(Throwable t, String format, Object... args) {
        if (isLoggable(6)) {
            Log.e(this.tag, toMessage(format, args), t);
        }
    }
}
