package org.tensorflow.mcr.road;

import android.os.AsyncTask;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.tensorflow.demo.Classifier;

public class DetectionTask extends AsyncTask<Void, Integer, Integer> {
    public CountDownLatch latch;
    public List<Classifier.Recognition> results = null;
    public TFBox tfBox;
    public float threshold;

    public DetectionTask(CountDownLatch latch2, TFBox tfBox2, float threshold2) {
        this.latch = latch2;
        this.tfBox = tfBox2;
        this.threshold = threshold2;
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Integer integer) {
        this.latch.countDown();
    }

    /* access modifiers changed from: protected */
    public Integer doInBackground(Void... voids) {
        try {
            this.results = this.tfBox.detector.recognizeImage(this.tfBox.image.bitmap);
            Iterator<Classifier.Recognition> iterator = this.results.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getConfidence().floatValue() < this.threshold) {
                    iterator.remove();
                }
            }
            return null;
        } catch (Exception e) {
            ThrowableExtension.printStackTrace(e);
            return null;
        }
    }
}
