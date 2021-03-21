package org.tensorflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class NativeLibrary {
    private static final boolean DEBUG = (System.getProperty("org.tensorflow.NativeLibrary.DEBUG") != null);
    private static final String JNI_LIBNAME = "tensorflow_jni";

    public static void load() {
        if (!isLoaded() && !tryLoadLibrary()) {
            String jniLibName = System.mapLibraryName(JNI_LIBNAME);
            String jniResourceName = makeResourceName(jniLibName);
            log("jniResourceName: " + jniResourceName);
            InputStream jniResource = NativeLibrary.class.getClassLoader().getResourceAsStream(jniResourceName);
            String frameworkLibName = maybeAdjustForMacOS(System.mapLibraryName("tensorflow_framework"));
            String frameworkResourceName = makeResourceName(frameworkLibName);
            log("frameworkResourceName: " + frameworkResourceName);
            InputStream frameworkResource = NativeLibrary.class.getClassLoader().getResourceAsStream(frameworkResourceName);
            if (jniResource == null) {
                throw new UnsatisfiedLinkError(String.format("Cannot find TensorFlow native library for OS: %s, architecture: %s. See https://github.com/tensorflow/tensorflow/tree/master/tensorflow/java/README.md for possible solutions (such as building the library from source). Additional information on attempts to find the native library can be obtained by adding org.tensorflow.NativeLibrary.DEBUG=1 to the system properties of the JVM.", new Object[]{m20os(), architecture()}));
            }
            try {
                File tempPath = createTemporaryDirectory();
                tempPath.deleteOnExit();
                String tempDirectory = tempPath.getCanonicalPath();
                if (frameworkResource != null) {
                    extractResource(frameworkResource, frameworkLibName, tempDirectory);
                } else {
                    log(frameworkResourceName + " not found. This is fine assuming " + jniResourceName + " is not built to depend on it.");
                }
                System.load(extractResource(jniResource, jniLibName, tempDirectory));
            } catch (IOException e) {
                throw new UnsatisfiedLinkError(String.format("Unable to extract native library into a temporary file (%s)", new Object[]{e.toString()}));
            }
        }
    }

    private static boolean tryLoadLibrary() {
        try {
            System.loadLibrary(JNI_LIBNAME);
            return true;
        } catch (UnsatisfiedLinkError e) {
            log("tryLoadLibraryFailed: " + e.getMessage());
            return false;
        }
    }

    private static boolean isLoaded() {
        try {
            TensorFlow.version();
            log("isLoaded: true");
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    private static String maybeAdjustForMacOS(String libFilename) {
        return (System.getProperty("os.name").contains("OS X") && NativeLibrary.class.getClassLoader().getResource(makeResourceName(libFilename)) == null && libFilename.endsWith(".dylib")) ? libFilename.substring(0, libFilename.length() - ".dylib".length()) + ".so" : libFilename;
    }

    private static String extractResource(InputStream resource, String resourceName, String extractToDirectory) throws IOException {
        File dst = new File(extractToDirectory, resourceName);
        dst.deleteOnExit();
        String dstPath = dst.toString();
        log("extracting native library to: " + dstPath);
        log(String.format("copied %d bytes to %s", new Object[]{Long.valueOf(copy(resource, dst)), dstPath}));
        return dstPath;
    }

    /* renamed from: os */
    private static String m20os() {
        String p = System.getProperty("os.name").toLowerCase();
        if (p.contains("linux")) {
            return "linux";
        }
        if (p.contains("os x") || p.contains("darwin")) {
            return "darwin";
        }
        if (p.contains("windows")) {
            return "windows";
        }
        return p.replaceAll("\\s", "");
    }

    private static String architecture() {
        String arch = System.getProperty("os.arch").toLowerCase();
        return arch.equals("amd64") ? "x86_64" : arch;
    }

    private static void log(String msg) {
        if (DEBUG) {
            System.err.println("org.tensorflow.NativeLibrary: " + msg);
        }
    }

    private static String makeResourceName(String baseName) {
        return "org/tensorflow/native/" + String.format("%s-%s/", new Object[]{m20os(), architecture()}) + baseName;
    }

    private static long copy(InputStream src, File dstFile) throws IOException {
        FileOutputStream dst = new FileOutputStream(dstFile);
        try {
            byte[] buffer = new byte[1048576];
            long ret = 0;
            while (true) {
                int n = src.read(buffer);
                if (n < 0) {
                    return ret;
                }
                dst.write(buffer, 0, n);
                ret += (long) n;
            }
        } finally {
            dst.close();
            src.close();
        }
    }

    private static File createTemporaryDirectory() {
        File baseDirectory = new File(System.getProperty("java.io.tmpdir"));
        String directoryName = "tensorflow_native_libraries-" + System.currentTimeMillis() + "-";
        for (int attempt = 0; attempt < 1000; attempt++) {
            File temporaryDirectory = new File(baseDirectory, directoryName + attempt);
            if (temporaryDirectory.mkdir()) {
                return temporaryDirectory;
            }
        }
        throw new IllegalStateException("Could not create a temporary directory (tried to make " + directoryName + "*) to extract TensorFlow native libraries.");
    }

    private NativeLibrary() {
    }
}
