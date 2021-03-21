package org.tensorflow.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;

public class TensorFlowMultiBoxDetector implements Classifier {
    private static final Logger LOGGER = new Logger();
    private static final int MAX_RESULTS = Integer.MAX_VALUE;
    private float[] boxPriors;
    private float[] floatValues;
    private int imageMean;
    private float imageStd;
    private TensorFlowInferenceInterface inferenceInterface;
    private String inputName;
    private int inputSize;
    private int[] intValues;
    private boolean logStats = false;
    private int numLocations;
    private float[] outputLocations;
    private String[] outputNames;
    private float[] outputScores;

    public static Classifier create(AssetManager assetManager, String modelFilename, String locationFilename, int imageMean2, float imageStd2, String inputName2, String outputLocationsName, String outputScoresName) {
        TensorFlowMultiBoxDetector d = new TensorFlowMultiBoxDetector();
        d.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        Graph g = d.inferenceInterface.graph();
        d.inputName = inputName2;
        Operation inputOp = g.operation(inputName2);
        if (inputOp == null) {
            throw new RuntimeException("Failed to find input Node '" + inputName2 + "'");
        }
        d.inputSize = (int) inputOp.output(0).shape().size(1);
        d.imageMean = imageMean2;
        d.imageStd = imageStd2;
        Operation outputOp = g.operation(outputScoresName);
        if (outputOp == null) {
            throw new RuntimeException("Failed to find output Node '" + outputScoresName + "'");
        }
        d.numLocations = (int) outputOp.output(0).shape().size(1);
        d.boxPriors = new float[(d.numLocations * 8)];
        try {
            d.loadCoderOptions(assetManager, locationFilename, d.boxPriors);
            d.outputNames = new String[]{outputLocationsName, outputScoresName};
            d.intValues = new int[(d.inputSize * d.inputSize)];
            d.floatValues = new float[(d.inputSize * d.inputSize * 3)];
            d.outputScores = new float[d.numLocations];
            d.outputLocations = new float[(d.numLocations * 4)];
            return d;
        } catch (IOException e) {
            throw new RuntimeException("Error initializing box priors from " + locationFilename);
        }
    }

    private TensorFlowMultiBoxDetector() {
    }

    private void loadCoderOptions(AssetManager assetManager, String locationFilename, float[] boxPriors2) throws IOException {
        InputStream is;
        if (locationFilename.startsWith("file:///android_asset/")) {
            is = assetManager.open(locationFilename.split("file:///android_asset/")[1]);
        } else {
            is = new FileInputStream(locationFilename);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int priorIndex = 0;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            StringTokenizer st = new StringTokenizer(line, ", ");
            while (st.hasMoreTokens()) {
                try {
                    int priorIndex2 = priorIndex + 1;
                    try {
                        boxPriors2[priorIndex] = Float.parseFloat(st.nextToken());
                        priorIndex = priorIndex2;
                    } catch (NumberFormatException e) {
                        priorIndex = priorIndex2;
                    }
                } catch (NumberFormatException e2) {
                }
            }
        }
        if (priorIndex != boxPriors2.length) {
            throw new RuntimeException("BoxPrior length mismatch: " + priorIndex + " vs " + boxPriors2.length);
        }
    }

    private float[] decodeLocationsEncoding(float[] locationEncoding) {
        float[] locations = new float[locationEncoding.length];
        boolean nonZero = false;
        for (int i = 0; i < this.numLocations; i++) {
            for (int j = 0; j < 4; j++) {
                float currEncoding = locationEncoding[(i * 4) + j];
                if (nonZero || currEncoding != 0.0f) {
                    nonZero = true;
                } else {
                    nonZero = false;
                }
                locations[(i * 4) + j] = Math.min(Math.max((currEncoding * this.boxPriors[(i * 8) + (j * 2) + 1]) + this.boxPriors[(i * 8) + (j * 2)], 0.0f), 1.0f);
            }
        }
        if (!nonZero) {
            LOGGER.mo6300w("No non-zero encodings; check log for inference errors.", new Object[0]);
        }
        return locations;
    }

    private float[] decodeScoresEncoding(float[] scoresEncoding) {
        float[] scores = new float[scoresEncoding.length];
        for (int i = 0; i < scoresEncoding.length; i++) {
            scores[i] = 1.0f / ((float) (1.0d + Math.exp((double) (-scoresEncoding[i]))));
        }
        return scores;
    }

    public List<Classifier.Recognition> recognizeImage(Bitmap bitmap) {
        Trace.beginSection("recognizeImage");
        Trace.beginSection("preprocessBitmap");
        bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < this.intValues.length; i++) {
            this.floatValues[(i * 3) + 0] = ((float) (((this.intValues[i] >> 16) & 255) - this.imageMean)) / this.imageStd;
            this.floatValues[(i * 3) + 1] = ((float) (((this.intValues[i] >> 8) & 255) - this.imageMean)) / this.imageStd;
            this.floatValues[(i * 3) + 2] = ((float) ((this.intValues[i] & 255) - this.imageMean)) / this.imageStd;
        }
        Trace.endSection();
        Trace.beginSection("feed");
        this.inferenceInterface.feed(this.inputName, this.floatValues, 1, (long) this.inputSize, (long) this.inputSize, 3);
        Trace.endSection();
        Trace.beginSection("run");
        this.inferenceInterface.run(this.outputNames, this.logStats);
        Trace.endSection();
        Trace.beginSection("fetch");
        float[] outputScoresEncoding = new float[this.numLocations];
        float[] outputLocationsEncoding = new float[(this.numLocations * 4)];
        this.inferenceInterface.fetch(this.outputNames[0], outputLocationsEncoding);
        this.inferenceInterface.fetch(this.outputNames[1], outputScoresEncoding);
        Trace.endSection();
        this.outputLocations = decodeLocationsEncoding(outputLocationsEncoding);
        this.outputScores = decodeScoresEncoding(outputScoresEncoding);
        PriorityQueue<Classifier.Recognition> pq = new PriorityQueue<>(1, new Comparator<Classifier.Recognition>() {
            public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                return Float.compare(rhs.getConfidence().floatValue(), lhs.getConfidence().floatValue());
            }
        });
        for (int i2 = 0; i2 < this.outputScores.length; i2++) {
            pq.add(new Classifier.Recognition("" + i2, (String) null, Float.valueOf(this.outputScores[i2]), new RectF(this.outputLocations[i2 * 4] * ((float) this.inputSize), this.outputLocations[(i2 * 4) + 1] * ((float) this.inputSize), this.outputLocations[(i2 * 4) + 2] * ((float) this.inputSize), this.outputLocations[(i2 * 4) + 3] * ((float) this.inputSize))));
        }
        ArrayList<Classifier.Recognition> recognitions = new ArrayList<>();
        for (int i3 = 0; i3 < Math.min(pq.size(), Integer.MAX_VALUE); i3++) {
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
