package org.tensorflow.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import org.tensorflow.Graph;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;

public class TensorFlowObjectDetectionAPIModel implements Classifier {
    private static final Logger LOGGER = new Logger();
    private static final int MAX_RESULTS = 100;
    private byte[] byteValues;
    private TensorFlowInferenceInterface inferenceInterface;
    private String inputName;
    private int inputSize;
    private int[] intValues;
    private Vector<String> labels = new Vector<>();
    private boolean logStats = false;
    private float[] outputClasses;
    private float[] outputLocations;
    private String[] outputNames;
    private float[] outputNumDetections;
    private float[] outputScores;

    public static Classifier create(AssetManager assetManager, String modelFilename, String labelFilename, int inputSize2) throws IOException {
        TensorFlowObjectDetectionAPIModel d = new TensorFlowObjectDetectionAPIModel();
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(labelFilename.split("file:///android_asset/")[1])));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            LOGGER.mo6300w(line, new Object[0]);
            d.labels.add(line);
        }
        br.close();
        d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        Graph g = d.inferenceInterface.graph();
        d.inputName = "image_tensor";
        if (g.operation(d.inputName) == null) {
            throw new RuntimeException("Failed to find input Node '" + d.inputName + "'");
        }
        d.inputSize = inputSize2;
        if (g.operation("detection_scores") == null) {
            throw new RuntimeException("Failed to find output Node 'detection_scores'");
        } else if (g.operation("detection_boxes") == null) {
            throw new RuntimeException("Failed to find output Node 'detection_boxes'");
        } else if (g.operation("detection_classes") == null) {
            throw new RuntimeException("Failed to find output Node 'detection_classes'");
        } else {
            d.outputNames = new String[]{"detection_boxes", "detection_scores", "detection_classes", "num_detections"};
            d.intValues = new int[(d.inputSize * d.inputSize)];
            d.byteValues = new byte[(d.inputSize * d.inputSize * 3)];
            d.outputScores = new float[100];
            d.outputLocations = new float[400];
            d.outputClasses = new float[100];
            d.outputNumDetections = new float[1];
            return d;
        }
    }

    private TensorFlowObjectDetectionAPIModel() {
    }

    public List<Classifier.Recognition> recognizeImage(Bitmap bitmap) {
        Trace.beginSection("recognizeImage");
        Trace.beginSection("preprocessBitmap");
        bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < this.intValues.length; i++) {
            this.byteValues[(i * 3) + 2] = (byte) (this.intValues[i] & 255);
            this.byteValues[(i * 3) + 1] = (byte) ((this.intValues[i] >> 8) & 255);
            this.byteValues[(i * 3) + 0] = (byte) ((this.intValues[i] >> 16) & 255);
        }
        Trace.endSection();
        Trace.beginSection("feed");
        this.inferenceInterface.feed(this.inputName, this.byteValues, 1, (long) this.inputSize, (long) this.inputSize, 3);
        Trace.endSection();
        Trace.beginSection("run");
        this.inferenceInterface.run(this.outputNames, this.logStats);
        Trace.endSection();
        Trace.beginSection("fetch");
        this.outputLocations = new float[400];
        this.outputScores = new float[100];
        this.outputClasses = new float[100];
        this.outputNumDetections = new float[1];
        this.inferenceInterface.fetch(this.outputNames[0], this.outputLocations);
        this.inferenceInterface.fetch(this.outputNames[1], this.outputScores);
        this.inferenceInterface.fetch(this.outputNames[2], this.outputClasses);
        this.inferenceInterface.fetch(this.outputNames[3], this.outputNumDetections);
        Trace.endSection();
        PriorityQueue<Classifier.Recognition> pq = new PriorityQueue<>(1, new Comparator<Classifier.Recognition>() {
            public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                return Float.compare(rhs.getConfidence().floatValue(), lhs.getConfidence().floatValue());
            }
        });
        for (int i2 = 0; i2 < this.outputScores.length; i2++) {
            pq.add(new Classifier.Recognition("" + i2, this.labels.get((int) this.outputClasses[i2]), Float.valueOf(this.outputScores[i2]), new RectF(this.outputLocations[(i2 * 4) + 1] * ((float) this.inputSize), this.outputLocations[i2 * 4] * ((float) this.inputSize), this.outputLocations[(i2 * 4) + 3] * ((float) this.inputSize), this.outputLocations[(i2 * 4) + 2] * ((float) this.inputSize))));
        }
        ArrayList<Classifier.Recognition> recognitions = new ArrayList<>();
        for (int i3 = 0; i3 < Math.min(pq.size(), 100); i3++) {
            recognitions.add(pq.poll());
        }
        Trace.endSection();
        return recognitions;
    }

    public void enableStatLogging(boolean logStats2) {
        this.logStats = logStats2;
    }

    public String getStatString() {
        return this.inferenceInterface.getStatString();
    }

    public void close() {
        this.inferenceInterface.close();
    }
}
