package org.tensorflow;

public final class TensorFlow {
    private static native void libraryDelete(long j);

    private static native long libraryLoad(String str);

    private static native byte[] libraryOpList(long j);

    public static native byte[] registeredOpList();

    public static native String version();

    public static byte[] loadLibrary(String filename) {
        try {
            long h = libraryLoad(filename);
            try {
                return libraryOpList(h);
            } finally {
                libraryDelete(h);
            }
        } catch (RuntimeException e) {
            throw new UnsatisfiedLinkError(e.getMessage());
        }
    }

    private TensorFlow() {
    }

    static void init() {
        NativeLibrary.load();
    }

    static {
        init();
    }
}
