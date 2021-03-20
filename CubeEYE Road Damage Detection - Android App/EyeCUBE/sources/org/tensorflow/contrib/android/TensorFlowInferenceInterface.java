package org.tensorflow.contrib.android;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;
import org.tensorflow.types.UInt8;

public class TensorFlowInferenceInterface {
    private static final String ASSET_FILE_PREFIX = "file:///android_asset/";
    private static final String TAG = "TensorFlowInferenceInterface";
    private List<String> feedNames = new ArrayList();
    private List<Tensor<?>> feedTensors = new ArrayList();
    private List<String> fetchNames = new ArrayList();
    private List<Tensor<?>> fetchTensors = new ArrayList();

    /* renamed from: g */
    private final Graph f21g;
    private final String modelName;
    private RunStats runStats;
    private Session.Runner runner;
    private final Session sess;

    public TensorFlowInferenceInterface(AssetManager assetManager, String model) {
        String aname;
        InputStream is;
        prepareNativeRuntime();
        this.modelName = model;
        this.f21g = new Graph();
        this.sess = new Session(this.f21g);
        this.runner = this.sess.runner();
        boolean hasAssetPrefix = model.startsWith(ASSET_FILE_PREFIX);
        if (hasAssetPrefix) {
            try {
                aname = model.split(ASSET_FILE_PREFIX)[1];
            } catch (IOException e) {
                if (hasAssetPrefix) {
                    throw new RuntimeException("Failed to load model from '" + model + "'", e);
                }
                try {
                    is = new FileInputStream(model);
                } catch (IOException e2) {
                    throw new RuntimeException("Failed to load model from '" + model + "'", e);
                }
            }
        } else {
            aname = model;
        }
        is = assetManager.open(aname);
        try {
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.beginSection("initializeTensorFlow");
                Trace.beginSection("readGraphDef");
            }
            byte[] graphDef = new byte[is.available()];
            int numBytesRead = is.read(graphDef);
            if (numBytesRead != graphDef.length) {
                throw new IOException("read error: read only " + numBytesRead + " of the graph, expected to read " + graphDef.length);
            }
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
            loadGraph(graphDef, this.f21g);
            is.close();
            Log.i(TAG, "Successfully loaded model from '" + model + "'");
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
        } catch (IOException e3) {
            throw new RuntimeException("Failed to load model from '" + model + "'", e3);
        }
    }

    public TensorFlowInferenceInterface(InputStream is) {
        int baosInitSize = 16384;
        prepareNativeRuntime();
        this.modelName = "";
        this.f21g = new Graph();
        this.sess = new Session(this.f21g);
        this.runner = this.sess.runner();
        try {
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.beginSection("initializeTensorFlow");
                Trace.beginSection("readGraphDef");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available() > 16384 ? is.available() : baosInitSize);
            byte[] buf = new byte[16384];
            while (true) {
                int numBytesRead = is.read(buf, 0, buf.length);
                if (numBytesRead == -1) {
                    break;
                }
                baos.write(buf, 0, numBytesRead);
            }
            byte[] graphDef = baos.toByteArray();
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
            loadGraph(graphDef, this.f21g);
            Log.i(TAG, "Successfully loaded model from the input stream");
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model from the input stream", e);
        }
    }

    public TensorFlowInferenceInterface(Graph g) {
        prepareNativeRuntime();
        this.modelName = "";
        this.f21g = g;
        this.sess = new Session(g);
        this.runner = this.sess.runner();
    }

    public void run(String[] outputNames) {
        run(outputNames, false);
    }

    public void run(String[] outputNames, boolean enableStats) {
        run(outputNames, enableStats, new String[0]);
    }

    public void run(String[] outputNames, boolean enableStats, String[] targetNodeNames) {
        closeFetches();
        for (String o : outputNames) {
            this.fetchNames.add(o);
            TensorId tid = TensorId.parse(o);
            this.runner.fetch(tid.name, tid.outputIndex);
        }
        for (String t : targetNodeNames) {
            this.runner.addTarget(t);
        }
        if (enableStats) {
            try {
                Session.Run r = this.runner.setOptions(RunStats.runOptions()).runAndFetchMetadata();
                this.fetchTensors = r.outputs;
                if (this.runStats == null) {
                    this.runStats = new RunStats();
                }
                this.runStats.add(r.metadata);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to run TensorFlow inference with inputs:[" + TextUtils.join(", ", this.feedNames) + "], outputs:[" + TextUtils.join(", ", this.fetchNames) + "]");
                throw e;
            } catch (Throwable th) {
                closeFeeds();
                this.runner = this.sess.runner();
                throw th;
            }
        } else {
            this.fetchTensors = this.runner.run();
        }
        closeFeeds();
        this.runner = this.sess.runner();
    }

    public Graph graph() {
        return this.f21g;
    }

    public Operation graphOperation(String operationName) {
        Operation operation = this.f21g.operation(operationName);
        if (operation != null) {
            return operation;
        }
        throw new RuntimeException("Node '" + operationName + "' does not exist in model '" + this.modelName + "'");
    }

    public String getStatString() {
        return this.runStats == null ? "" : this.runStats.summary();
    }

    public void close() {
        closeFeeds();
        closeFetches();
        this.sess.close();
        this.f21g.close();
        if (this.runStats != null) {
            this.runStats.close();
        }
        this.runStats = null;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public void feed(String inputName, boolean[] src, long... dims) {
        byte[] b = new byte[src.length];
        for (int i = 0; i < src.length; i++) {
            b[i] = src[i] ? (byte) 1 : 0;
        }
        addFeed(inputName, Tensor.create(Boolean.class, dims, ByteBuffer.wrap(b)));
    }

    public void feed(String inputName, float[] src, long... dims) {
        addFeed(inputName, Tensor.create(dims, FloatBuffer.wrap(src)));
    }

    public void feed(String inputName, int[] src, long... dims) {
        addFeed(inputName, Tensor.create(dims, IntBuffer.wrap(src)));
    }

    public void feed(String inputName, long[] src, long... dims) {
        addFeed(inputName, Tensor.create(dims, LongBuffer.wrap(src)));
    }

    public void feed(String inputName, double[] src, long... dims) {
        addFeed(inputName, Tensor.create(dims, DoubleBuffer.wrap(src)));
    }

    public void feed(String inputName, byte[] src, long... dims) {
        addFeed(inputName, Tensor.create(UInt8.class, dims, ByteBuffer.wrap(src)));
    }

    public void feedString(String inputName, byte[] src) {
        addFeed(inputName, Tensors.create(src));
    }

    public void feedString(String inputName, byte[][] src) {
        addFeed(inputName, Tensors.create(src));
    }

    public void feed(String inputName, FloatBuffer src, long... dims) {
        addFeed(inputName, Tensor.create(dims, src));
    }

    public void feed(String inputName, IntBuffer src, long... dims) {
        addFeed(inputName, Tensor.create(dims, src));
    }

    public void feed(String inputName, LongBuffer src, long... dims) {
        addFeed(inputName, Tensor.create(dims, src));
    }

    public void feed(String inputName, DoubleBuffer src, long... dims) {
        addFeed(inputName, Tensor.create(dims, src));
    }

    public void feed(String inputName, ByteBuffer src, long... dims) {
        addFeed(inputName, Tensor.create(UInt8.class, dims, src));
    }

    public void fetch(String outputName, float[] dst) {
        fetch(outputName, FloatBuffer.wrap(dst));
    }

    public void fetch(String outputName, int[] dst) {
        fetch(outputName, IntBuffer.wrap(dst));
    }

    public void fetch(String outputName, long[] dst) {
        fetch(outputName, LongBuffer.wrap(dst));
    }

    public void fetch(String outputName, double[] dst) {
        fetch(outputName, DoubleBuffer.wrap(dst));
    }

    public void fetch(String outputName, byte[] dst) {
        fetch(outputName, ByteBuffer.wrap(dst));
    }

    public void fetch(String outputName, FloatBuffer dst) {
        getTensor(outputName).writeTo(dst);
    }

    public void fetch(String outputName, IntBuffer dst) {
        getTensor(outputName).writeTo(dst);
    }

    public void fetch(String outputName, LongBuffer dst) {
        getTensor(outputName).writeTo(dst);
    }

    public void fetch(String outputName, DoubleBuffer dst) {
        getTensor(outputName).writeTo(dst);
    }

    public void fetch(String outputName, ByteBuffer dst) {
        getTensor(outputName).writeTo(dst);
    }

    private void prepareNativeRuntime() {
        Log.i(TAG, "Checking to see if TensorFlow native methods are already loaded");
        try {
            new RunStats();
            Log.i(TAG, "TensorFlow native methods already loaded");
        } catch (UnsatisfiedLinkError e) {
            Log.i(TAG, "TensorFlow native methods not found, attempting to load via tensorflow_inference");
            try {
                System.loadLibrary("tensorflow_inference");
                Log.i(TAG, "Successfully loaded TensorFlow native methods (RunStats error may be ignored)");
            } catch (UnsatisfiedLinkError e2) {
                throw new RuntimeException("Native TF methods not found; check that the correct native libraries are present in the APK.");
            }
        }
    }

    private void loadGraph(byte[] graphDef, Graph g) throws IOException {
        long startMs = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= 18) {
            Trace.beginSection("importGraphDef");
        }
        try {
            g.importGraphDef(graphDef);
            if (Build.VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }
            Log.i(TAG, "Model load took " + (System.currentTimeMillis() - startMs) + "ms, TensorFlow version: " + TensorFlow.version());
        } catch (IllegalArgumentException e) {
            throw new IOException("Not a valid TensorFlow Graph serialization: " + e.getMessage());
        }
    }

    private void addFeed(String inputName, Tensor<?> t) {
        TensorId tid = TensorId.parse(inputName);
        this.runner.feed(tid.name, tid.outputIndex, t);
        this.feedNames.add(inputName);
        this.feedTensors.add(t);
    }

    private static class TensorId {
        String name;
        int outputIndex;

        private TensorId() {
        }

        public static TensorId parse(String name2) {
            TensorId tid = new TensorId();
            int colonIndex = name2.lastIndexOf(58);
            if (colonIndex < 0) {
                tid.outputIndex = 0;
                tid.name = name2;
            } else {
                try {
                    tid.outputIndex = Integer.parseInt(name2.substring(colonIndex + 1));
                    tid.name = name2.substring(0, colonIndex);
                } catch (NumberFormatException e) {
                    tid.outputIndex = 0;
                    tid.name = name2;
                }
            }
            return tid;
        }
    }

    private Tensor<?> getTensor(String outputName) {
        int i = 0;
        for (String n : this.fetchNames) {
            if (n.equals(outputName)) {
                return this.fetchTensors.get(i);
            }
            i++;
        }
        throw new RuntimeException("Node '" + outputName + "' was not provided to run(), so it cannot be read");
    }

    private void closeFeeds() {
        for (Tensor<?> t : this.feedTensors) {
            t.close();
        }
        this.feedTensors.clear();
        this.feedNames.clear();
    }

    private void closeFetches() {
        for (Tensor<?> t : this.fetchTensors) {
            t.close();
        }
        this.fetchTensors.clear();
        this.fetchNames.clear();
    }
}
