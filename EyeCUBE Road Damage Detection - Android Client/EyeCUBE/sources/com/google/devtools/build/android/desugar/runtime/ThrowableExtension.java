package com.google.devtools.build.android.desugar.runtime;

import java.io.Closeable;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public final class ThrowableExtension {
    private static final String ANDROID_OS_BUILD_VERSION = "android.os.Build$VERSION";
    static final int API_LEVEL;
    static final AbstractDesugaringStrategy STRATEGY;
    public static final String SYSTEM_PROPERTY_TWR_DISABLE_MIMIC = "com.google.devtools.build.android.desugar.runtime.twr_disable_mimic";

    /* JADX WARNING: Removed duplicated region for block: B:19:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0018  */
    static {
        /*
            r0 = 0
            java.lang.Integer r0 = readApiLevelFromBuildVersion()     // Catch:{ Throwable -> 0x002e }
            if (r0 == 0) goto L_0x001c
            int r3 = r0.intValue()     // Catch:{ Throwable -> 0x002e }
            r4 = 19
            if (r3 < r4) goto L_0x001c
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension$ReuseDesugaringStrategy r2 = new com.google.devtools.build.android.desugar.runtime.ThrowableExtension$ReuseDesugaringStrategy     // Catch:{ Throwable -> 0x002e }
            r2.<init>()     // Catch:{ Throwable -> 0x002e }
        L_0x0014:
            STRATEGY = r2
            if (r0 != 0) goto L_0x005e
            r3 = 1
        L_0x0019:
            API_LEVEL = r3
            return
        L_0x001c:
            boolean r3 = useMimicStrategy()     // Catch:{ Throwable -> 0x002e }
            if (r3 == 0) goto L_0x0028
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension$MimicDesugaringStrategy r2 = new com.google.devtools.build.android.desugar.runtime.ThrowableExtension$MimicDesugaringStrategy     // Catch:{ Throwable -> 0x002e }
            r2.<init>()     // Catch:{ Throwable -> 0x002e }
            goto L_0x0014
        L_0x0028:
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension$NullDesugaringStrategy r2 = new com.google.devtools.build.android.desugar.runtime.ThrowableExtension$NullDesugaringStrategy     // Catch:{ Throwable -> 0x002e }
            r2.<init>()     // Catch:{ Throwable -> 0x002e }
            goto L_0x0014
        L_0x002e:
            r1 = move-exception
            java.io.PrintStream r3 = java.lang.System.err
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "An error has occured when initializing the try-with-resources desuguring strategy. The default strategy "
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.Class<com.google.devtools.build.android.desugar.runtime.ThrowableExtension$NullDesugaringStrategy> r5 = com.google.devtools.build.android.desugar.runtime.ThrowableExtension.NullDesugaringStrategy.class
            java.lang.String r5 = r5.getName()
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r5 = "will be used. The error is: "
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.println(r4)
            java.io.PrintStream r3 = java.lang.System.err
            r1.printStackTrace(r3)
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension$NullDesugaringStrategy r2 = new com.google.devtools.build.android.desugar.runtime.ThrowableExtension$NullDesugaringStrategy
            r2.<init>()
            goto L_0x0014
        L_0x005e:
            int r3 = r0.intValue()
            goto L_0x0019
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.devtools.build.android.desugar.runtime.ThrowableExtension.<clinit>():void");
    }

    public static AbstractDesugaringStrategy getStrategy() {
        return STRATEGY;
    }

    public static void addSuppressed(Throwable receiver, Throwable suppressed) {
        STRATEGY.addSuppressed(receiver, suppressed);
    }

    public static Throwable[] getSuppressed(Throwable receiver) {
        return STRATEGY.getSuppressed(receiver);
    }

    public static void printStackTrace(Throwable receiver) {
        STRATEGY.printStackTrace(receiver);
    }

    public static void printStackTrace(Throwable receiver, PrintWriter writer) {
        STRATEGY.printStackTrace(receiver, writer);
    }

    public static void printStackTrace(Throwable receiver, PrintStream stream) {
        STRATEGY.printStackTrace(receiver, stream);
    }

    public static void closeResource(Throwable throwable, Object resource) throws Throwable {
        Throwable e;
        Exception e2;
        if (resource != null) {
            try {
                if (API_LEVEL >= 19) {
                    ((AutoCloseable) resource).close();
                    return;
                } else if (resource instanceof Closeable) {
                    ((Closeable) resource).close();
                    return;
                } else {
                    resource.getClass().getMethod("close", new Class[0]).invoke(resource, new Object[0]);
                    return;
                }
            } catch (NoSuchMethodException e3) {
                e2 = e3;
            } catch (SecurityException e4) {
                e2 = e4;
            } catch (IllegalAccessException e5) {
                e = e5;
                throw new AssertionError("Fail to call close() on " + resource.getClass(), e);
            } catch (IllegalArgumentException e6) {
                e = e6;
                throw new AssertionError("Fail to call close() on " + resource.getClass(), e);
            } catch (ExceptionInInitializerError e7) {
                e = e7;
                throw new AssertionError("Fail to call close() on " + resource.getClass(), e);
            } catch (InvocationTargetException e8) {
                throw e8.getCause();
            } catch (Throwable e9) {
                if (throwable != null) {
                    addSuppressed(throwable, e9);
                    throw throwable;
                }
                throw e9;
            }
        } else {
            return;
        }
        throw new AssertionError(resource.getClass() + " does not have a close() method.", e2);
    }

    private static boolean useMimicStrategy() {
        return !Boolean.getBoolean(SYSTEM_PROPERTY_TWR_DISABLE_MIMIC);
    }

    private static Integer readApiLevelFromBuildVersion() {
        try {
            return (Integer) Class.forName(ANDROID_OS_BUILD_VERSION).getField("SDK_INT").get((Object) null);
        } catch (Exception e) {
            System.err.println("Failed to retrieve value from android.os.Build$VERSION.SDK_INT due to the following exception.");
            e.printStackTrace(System.err);
            return null;
        }
    }

    static abstract class AbstractDesugaringStrategy {
        protected static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];

        public abstract void addSuppressed(Throwable th, Throwable th2);

        public abstract Throwable[] getSuppressed(Throwable th);

        public abstract void printStackTrace(Throwable th);

        public abstract void printStackTrace(Throwable th, PrintStream printStream);

        public abstract void printStackTrace(Throwable th, PrintWriter printWriter);

        AbstractDesugaringStrategy() {
        }
    }

    static final class ReuseDesugaringStrategy extends AbstractDesugaringStrategy {
        ReuseDesugaringStrategy() {
        }

        public void addSuppressed(Throwable receiver, Throwable suppressed) {
            receiver.addSuppressed(suppressed);
        }

        public Throwable[] getSuppressed(Throwable receiver) {
            return receiver.getSuppressed();
        }

        public void printStackTrace(Throwable receiver) {
            receiver.printStackTrace();
        }

        public void printStackTrace(Throwable receiver, PrintStream stream) {
            receiver.printStackTrace(stream);
        }

        public void printStackTrace(Throwable receiver, PrintWriter writer) {
            receiver.printStackTrace(writer);
        }
    }

    static final class MimicDesugaringStrategy extends AbstractDesugaringStrategy {
        static final String SUPPRESSED_PREFIX = "Suppressed: ";
        private final ConcurrentWeakIdentityHashMap map = new ConcurrentWeakIdentityHashMap();

        MimicDesugaringStrategy() {
        }

        public void addSuppressed(Throwable receiver, Throwable suppressed) {
            if (suppressed == receiver) {
                throw new IllegalArgumentException("Self suppression is not allowed.", suppressed);
            } else if (suppressed == null) {
                throw new NullPointerException("The suppressed exception cannot be null.");
            } else {
                this.map.get(receiver, true).add(suppressed);
            }
        }

        public Throwable[] getSuppressed(Throwable receiver) {
            List<Throwable> list = this.map.get(receiver, false);
            if (list == null || list.isEmpty()) {
                return EMPTY_THROWABLE_ARRAY;
            }
            return (Throwable[]) list.toArray(EMPTY_THROWABLE_ARRAY);
        }

        public void printStackTrace(Throwable receiver) {
            receiver.printStackTrace();
            List<Throwable> suppressedList = this.map.get(receiver, false);
            if (suppressedList != null) {
                synchronized (suppressedList) {
                    for (Throwable suppressed : suppressedList) {
                        System.err.print(SUPPRESSED_PREFIX);
                        suppressed.printStackTrace();
                    }
                }
            }
        }

        public void printStackTrace(Throwable receiver, PrintStream stream) {
            receiver.printStackTrace(stream);
            List<Throwable> suppressedList = this.map.get(receiver, false);
            if (suppressedList != null) {
                synchronized (suppressedList) {
                    for (Throwable suppressed : suppressedList) {
                        stream.print(SUPPRESSED_PREFIX);
                        suppressed.printStackTrace(stream);
                    }
                }
            }
        }

        public void printStackTrace(Throwable receiver, PrintWriter writer) {
            receiver.printStackTrace(writer);
            List<Throwable> suppressedList = this.map.get(receiver, false);
            if (suppressedList != null) {
                synchronized (suppressedList) {
                    for (Throwable suppressed : suppressedList) {
                        writer.print(SUPPRESSED_PREFIX);
                        suppressed.printStackTrace(writer);
                    }
                }
            }
        }
    }

    static final class ConcurrentWeakIdentityHashMap {
        private final ConcurrentHashMap<WeakKey, List<Throwable>> map = new ConcurrentHashMap<>(16, 0.75f, 10);
        private final ReferenceQueue<Throwable> referenceQueue = new ReferenceQueue<>();

        ConcurrentWeakIdentityHashMap() {
        }

        public List<Throwable> get(Throwable throwable, boolean createOnAbsence) {
            deleteEmptyKeys();
            List<Throwable> list = this.map.get(new WeakKey(throwable, (ReferenceQueue<Throwable>) null));
            if (!createOnAbsence) {
                return list;
            }
            if (list != null) {
                return list;
            }
            List<Throwable> newValue = new Vector<>(2);
            List<Throwable> list2 = this.map.putIfAbsent(new WeakKey(throwable, this.referenceQueue), newValue);
            return list2 != null ? list2 : newValue;
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.map.size();
        }

        /* access modifiers changed from: package-private */
        public void deleteEmptyKeys() {
            Reference<? extends Throwable> poll = this.referenceQueue.poll();
            while (poll != null) {
                this.map.remove(poll);
                poll = this.referenceQueue.poll();
            }
        }

        private static final class WeakKey extends WeakReference<Throwable> {
            private final int hash;

            public WeakKey(Throwable referent, ReferenceQueue<Throwable> q) {
                super(referent, q);
                if (referent == null) {
                    throw new NullPointerException("The referent cannot be null");
                }
                this.hash = System.identityHashCode(referent);
            }

            public int hashCode() {
                return this.hash;
            }

            public boolean equals(Object obj) {
                if (obj == null || obj.getClass() != getClass()) {
                    return false;
                }
                if (this == obj) {
                    return true;
                }
                WeakKey other = (WeakKey) obj;
                if (this.hash == other.hash && get() == other.get()) {
                    return true;
                }
                return false;
            }
        }
    }

    static final class NullDesugaringStrategy extends AbstractDesugaringStrategy {
        NullDesugaringStrategy() {
        }

        public void addSuppressed(Throwable receiver, Throwable suppressed) {
        }

        public Throwable[] getSuppressed(Throwable receiver) {
            return EMPTY_THROWABLE_ARRAY;
        }

        public void printStackTrace(Throwable receiver) {
            receiver.printStackTrace();
        }

        public void printStackTrace(Throwable receiver, PrintStream stream) {
            receiver.printStackTrace(stream);
        }

        public void printStackTrace(Throwable receiver, PrintWriter writer) {
            receiver.printStackTrace(writer);
        }
    }
}
