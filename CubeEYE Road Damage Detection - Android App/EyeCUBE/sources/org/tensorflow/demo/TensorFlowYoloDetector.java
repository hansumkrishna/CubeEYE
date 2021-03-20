package org.tensorflow.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.env.SplitTimer;

public class TensorFlowYoloDetector implements Classifier {
    private static final double[] ANCHORS = {1.08d, 1.19d, 3.42d, 4.41d, 6.63d, 11.38d, 9.42d, 5.11d, 16.62d, 10.52d};
    private static final String[] LABELS = {"aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow", "diningtable", "dog", "horse", "motorbike", "person", "pottedplant", "sheep", "sofa", "train", "tvmonitor"};
    private static final Logger LOGGER = new Logger();
    private static final int MAX_RESULTS = 5;
    private static final int NUM_BOXES_PER_BLOCK = 5;
    private static final int NUM_CLASSES = 20;
    private int blockSize;
    private float[] floatValues;
    private TensorFlowInferenceInterface inferenceInterface;
    private String inputName;
    private int inputSize;
    private int[] intValues;
    private boolean logStats = false;
    private String[] outputNames;

    public static Classifier create(AssetManager assetManager, String modelFilename, int inputSize2, String inputName2, String outputName, int blockSize2) {
        TensorFlowYoloDetector d = new TensorFlowYoloDetector();
        d.inputName = inputName2;
        d.inputSize = inputSize2;
        d.outputNames = outputName.split(",");
        d.intValues = new int[(inputSize2 * inputSize2)];
        d.floatValues = new float[(inputSize2 * inputSize2 * 3)];
        d.blockSize = blockSize2;
        d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        return d;
    }

    private TensorFlowYoloDetector() {
    }

    private float expit(float x) {
        return (float) (1.0d / (Math.exp((double) (-x)) + 1.0d));
    }

    private void softmax(float[] vals) {
        float max = Float.NEGATIVE_INFINITY;
        for (float val : vals) {
            max = Math.max(max, val);
        }
        float sum = 0.0f;
        for (int i = 0; i < vals.length; i++) {
            vals[i] = (float) Math.exp((double) (vals[i] - max));
            sum += vals[i];
        }
        for (int i2 = 0; i2 < vals.length; i2++) {
            vals[i2] = vals[i2] / sum;
        }
    }

    public List<Classifier.Recognition> recognizeImage(Bitmap bitmap) {
        SplitTimer splitTimer = new SplitTimer("recognizeImage");
        Trace.beginSection("recognizeImage");
        Trace.beginSection("preprocessBitmap");
        bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < this.intValues.length; i++) {
            this.floatValues[(i * 3) + 0] = ((float) ((this.intValues[i] >> 16) & 255)) / 255.0f;
            this.floatValues[(i * 3) + 1] = ((float) ((this.intValues[i] >> 8) & 255)) / 255.0f;
            this.floatValues[(i * 3) + 2] = ((float) (this.intValues[i] & 255)) / 255.0f;
        }
        Trace.endSection();
        Trace.beginSection("feed");
        this.inferenceInterface.feed(this.inputName, this.floatValues, 1, (long) this.inputSize, (long) this.inputSize, 3);
        Trace.endSection();
        splitTimer.endSplit("ready for inference");
        Trace.beginSection("run");
        this.inferenceInterface.run(this.outputNames, this.logStats);
        Trace.endSection();
        splitTimer.endSplit("ran inference");
        Trace.beginSection("fetch");
        int gridWidth = bitmap.getWidth() / this.blockSize;
        int gridHeight = bitmap.getHeight() / this.blockSize;
        float[] output = new float[(gridWidth * gridHeight * 25 * 5)];
        this.inferenceInterface.fetch(this.outputNames[0], output);
        Trace.endSection();
        PriorityQueue priorityQueue = new PriorityQueue(1, new Comparator<Classifier.Recognition>() {
            public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                return Float.compare(rhs.getConfidence().floatValue(), lhs.getConfidence().floatValue());
            }
        });
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                for (int b = 0; b < 5; b++) {
                    int offset = (gridWidth * 125 * y) + (x * 125) + (b * 25);
                    float xPos = (((float) x) + expit(output[offset + 0])) * ((float) this.blockSize);
                    float yPos = (((float) y) + expit(output[offset + 1])) * ((float) this.blockSize);
                    float w = ((float) (Math.exp((double) output[offset + 2]) * ANCHORS[(b * 2) + 0])) * ((float) this.blockSize);
                    float h = ((float) (Math.exp((double) output[offset + 3]) * ANCHORS[(b * 2) + 1])) * ((float) this.blockSize);
                    RectF rectF = new RectF(Math.max(0.0f, xPos - (w / 2.0f)), Math.max(0.0f, yPos - (h / 2.0f)), Math.min((float) (bitmap.getWidth() - 1), (w / 2.0f) + xPos), Math.min((float) (bitmap.getHeight() - 1), (h / 2.0f) + yPos));
                    float confidence = expit(output[offset + 4]);
                    int detectedClass = -1;
                    float maxClass = 0.0f;
                    float[] classes = new float[20];
                    for (int c = 0; c < 20; c++) {
                        classes[c] = output[offset + 5 + c];
                    }
                    softmax(classes);
                    for (int c2 = 0; c2 < 20; c2++) {
                        if (classes[c2] > maxClass) {
                            detectedClass = c2;
                            maxClass = classes[c2];
                        }
                    }
                    float confidenceInClass = maxClass * confidence;
                    if (((double) confidenceInClass) > 0.01d) {
                        LOGGER.mo6294i("%s (%d) %f %s", LABELS[detectedClass], Integer.valueOf(detectedClass), Float.valueOf(confidenceInClass), rectF);
                        priorityQueue.add(new Classifier.Recognition("" + offset, LABELS[detectedClass], Float.valueOf(confidenceInClass), rectF));
                    }
                }
            }
        }
        splitTimer.endSplit("decoded results");
        ArrayList<Classifier.Recognition> recognitions = new ArrayList<>();
        for (int i2 = 0; i2 < Math.min(priorityQueue.size(), 5); i2++) {
            recognitions.add((Classifier.Recognition) priorityQueue.poll());
        }
        Trace.endSection();
        splitTimer.endSplit("processed results");
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
