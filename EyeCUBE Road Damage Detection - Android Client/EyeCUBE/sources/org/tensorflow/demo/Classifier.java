package org.tensorflow.demo;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.List;

public interface Classifier {
    void close();

    void enableStatLogging(boolean z);

    String getStatString();

    List<Recognition> recognizeImage(Bitmap bitmap);

    public static class Recognition {
        private final Float confidence;

        /* renamed from: id */
        private final String f22id;
        private RectF location;
        private final String title;

        public Recognition(String id, String title2, Float confidence2, RectF location2) {
            this.f22id = id;
            this.title = title2;
            this.confidence = confidence2;
            this.location = location2;
        }

        public String getId() {
            return this.f22id;
        }

        public String getTitle() {
            return this.title;
        }

        public Float getConfidence() {
            return this.confidence;
        }

        public RectF getLocation() {
            return new RectF(this.location);
        }

        public void setLocation(RectF location2) {
            this.location = location2;
        }

        public String toString() {
            String resultString = "";
            if (this.f22id != null) {
                resultString = resultString + "[" + this.f22id + "] ";
            }
            if (this.title != null) {
                resultString = resultString + this.title + " ";
            }
            if (this.confidence != null) {
                resultString = resultString + String.format("(%.1f%%) ", new Object[]{Float.valueOf(this.confidence.floatValue() * 100.0f)});
            }
            if (this.location != null) {
                resultString = resultString + this.location + " ";
            }
            return resultString.trim();
        }
    }
}
