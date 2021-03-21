package org.tensorflow.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.support.p000v4.p002os.EnvironmentCompat;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.Classifier;

public class TensorFlowImageClassifier implements Classifier {
    private static final int MAX_RESULTS = 3;
    private static final String TAG = "TensorFlowImageClassifier";
    private static final float THRESHOLD = 0.1f;
    private float[] floatValues;
    private int imageMean;
    private float imageStd;
    private TensorFlowInferenceInterface inferenceInterface;
    private String inputName;
    private int inputSize;
    private int[] intValues;
    private Vector<String> labels = new Vector<>();
    private boolean logStats = false;
    private String outputName;
    private String[] outputNames;
    private float[] outputs;

    private TensorFlowImageClassifier() {
    }

    public static Classifier create(AssetManager assetManager, String modelFilename, String labelFilename, int inputSize2, int imageMean2, float imageStd2, String inputName2, String outputName2) {
        TensorFlowImageClassifier c = new TensorFlowImageClassifier();
        c.inputName = inputName2;
        c.outputName = outputName2;
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        Log.i(TAG, "Reading labels from: " + actualFilename);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            while (true) {
                try {
                    String line = br.readLine();
                    if (line != null) {
                        c.labels.add(line);
                    } else {
                        br.close();
                        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
                        int numClasses = (int) c.inferenceInterface.graphOperation(outputName2).output(0).shape().size(1);
                        Log.i(TAG, "Read " + c.labels.size() + " labels, output layer size is " + numClasses);
                        c.inputSize = inputSize2;
                        c.imageMean = imageMean2;
                        c.imageStd = imageStd2;
                        c.outputNames = new String[]{outputName2};
                        c.intValues = new int[(inputSize2 * inputSize2)];
                        c.floatValues = new float[(inputSize2 * inputSize2 * 3)];
                        c.outputs = new float[numClasses];
                        return c;
                    }
                } catch (IOException e) {
                    e = e;
                    BufferedReader bufferedReader = br;
                    throw new RuntimeException("Problem reading label file!", e);
                }
            }
        } catch (IOException e2) {
            e = e2;
            throw new RuntimeException("Problem reading label file!", e);
        }
    }

    public List<Classifier.Recognition> recognizeImage(Bitmap bitmap) {
        Trace.beginSection("recognizeImage");
        Trace.beginSection("preprocessBitmap");
        bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < this.intValues.length; i++) {
            int val = this.intValues[i];
            this.floatValues[(i * 3) + 0] = ((float) (((val >> 16) & 255) - this.imageMean)) / this.imageStd;
            this.floatValues[(i * 3) + 1] = ((float) (((val >> 8) & 255) - this.imageMean)) / this.imageStd;
            this.floatValues[(i * 3) + 2] = ((float) ((val & 255) - this.imageMean)) / this.imageStd;
        }
        Trace.endSection();
        Trace.beginSection("feed");
        this.inferenceInterface.feed(this.inputName, this.floatValues, 1, (long) this.inputSize, (long) this.inputSize, 3);
        Trace.endSection();
        Trace.beginSection("run");
        this.inferenceInterface.run(this.outputNames, this.logStats);
        Trace.endSection();
        Trace.beginSection("fetch");
        this.inferenceInterface.fetch(this.outputName, this.outputs);
        Trace.endSection();
        PriorityQueue<Classifier.Recognition> pq = new PriorityQueue<>(3, new Comparator<Classifier.Recognition>() {
            public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                return Float.compare(rhs.getConfidence().floatValue(), lhs.getConfidence().floatValue());
            }
        });
        int i2 = 0;
        while (i2 < this.outputs.length) {
            if (this.outputs[i2] > THRESHOLD) {
                pq.add(new Classifier.Recognition("" + i2, this.labels.size() > i2 ? this.labels.get(i2) : EnvironmentCompat.MEDIA_UNKNOWN, Float.valueOf(this.outputs[i2]), (RectF) null));
            }
            i2++;
        }
        ArrayList<Classifier.Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), 3);
        for (int i3 = 0; i3 < recognitionsSize; i3++) {
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
