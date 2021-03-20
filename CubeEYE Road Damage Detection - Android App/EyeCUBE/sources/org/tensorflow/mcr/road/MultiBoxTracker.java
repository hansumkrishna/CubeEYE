package org.tensorflow.mcr.road;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.p000v4.internal.view.SupportMenu;
import android.text.TextUtils;
import android.util.TypedValue;
import java.util.LinkedList;
import java.util.List;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.Logger;

public class MultiBoxTracker {
    private static final float TEXT_SIZE_DIP = 18.0f;
    private final BorderedText borderedText;
    private final Paint boxPaint = new Paint();
    private List<RectF> cropAreas;
    private int frameHeight;
    private Matrix frameToCanvasMatrix;
    private int frameWidth;
    private final Paint idlePaint = new Paint();
    private final Logger logger = new Logger();
    private List<Classifier.Recognition> objects = new LinkedList();
    private boolean processing = false;
    private boolean rectMode = false;
    private final Paint runningPaint = new Paint();
    private int sensorOrientation;
    private final float textSizePx;
    private long timestamp;

    public MultiBoxTracker(Context context) {
        this.boxPaint.setStyle(Paint.Style.STROKE);
        this.boxPaint.setStrokeWidth(12.0f);
        this.boxPaint.setStrokeCap(Paint.Cap.ROUND);
        this.boxPaint.setStrokeJoin(Paint.Join.ROUND);
        this.boxPaint.setStrokeMiter(100.0f);
        this.boxPaint.setColor(-16711936);
        this.idlePaint.setStyle(Paint.Style.STROKE);
        this.idlePaint.setStrokeWidth(20.0f);
        this.idlePaint.setColor(-16776961);
        this.runningPaint.setStyle(Paint.Style.STROKE);
        this.runningPaint.setStrokeWidth(20.0f);
        this.runningPaint.setColor(SupportMenu.CATEGORY_MASK);
        this.textSizePx = TypedValue.applyDimension(1, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
        this.borderedText = new BorderedText(this.textSizePx);
        this.borderedText.setTextAlign(Paint.Align.CENTER);
    }

    private Matrix getFrameToCanvasMatrix() {
        return this.frameToCanvasMatrix;
    }

    public synchronized void trackResults(List<Classifier.Recognition> results, byte[] frame, long timestamp2) {
        this.logger.mo6294i("Processing %d results from %d", Integer.valueOf(results.size()), Long.valueOf(timestamp2));
        processResults(timestamp2, results, frame);
    }

    public synchronized void setCropAreas(List<RectF> cropAreas2) {
        this.cropAreas = cropAreas2;
    }

    public synchronized void draw(Canvas canvas) {
        List<RectF> displayRect;
        String labelString;
        Paint paint;
        boolean rotated = this.sensorOrientation % 180 == 90;
        float multiplier = Math.min(((float) canvas.getHeight()) / ((float) (rotated ? this.frameWidth : this.frameHeight)), ((float) canvas.getWidth()) / ((float) (rotated ? this.frameHeight : this.frameWidth)));
        this.frameToCanvasMatrix = ImageMatrixUtils.getTransformationMatrix(this.frameWidth, this.frameHeight, (int) (((float) (rotated ? this.frameHeight : this.frameWidth)) * multiplier), (int) (((float) (rotated ? this.frameWidth : this.frameHeight)) * multiplier), this.sensorOrientation, false);
        int areaSize = this.cropAreas.size();
        if (this.rectMode) {
            displayRect = this.cropAreas.subList(0, areaSize - 1);
        } else {
            displayRect = this.cropAreas.subList(areaSize - 1, areaSize);
        }
        for (RectF rect : displayRect) {
            RectF paintRect = new RectF();
            getFrameToCanvasMatrix().mapRect(paintRect, rect);
            if (this.processing) {
                paint = this.runningPaint;
            } else {
                paint = this.idlePaint;
            }
            canvas.drawRect(paintRect, paint);
        }
        if (this.processing) {
            long millis = System.currentTimeMillis() - this.timestamp;
            this.borderedText.drawText(canvas, (float) (canvas.getWidth() / 2), 100.0f, String.format("%02d:%02d", new Object[]{Long.valueOf((millis / 60000) % 60), Long.valueOf((millis / 1000) % 60)}));
        }
        for (Classifier.Recognition recognition : this.objects) {
            RectF trackedPos = recognition.getLocation();
            getFrameToCanvasMatrix().mapRect(trackedPos);
            float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
            canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, this.boxPaint);
            if (!TextUtils.isEmpty(recognition.getTitle())) {
                labelString = String.format("%s %.2f", new Object[]{recognition.getTitle(), recognition.getConfidence()});
            } else {
                labelString = String.format("%.2f", new Object[]{recognition.getConfidence()});
            }
            this.borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.bottom, labelString);
        }
    }

    public synchronized void onFrame(int w, int h, int rowStride, int sensorOrientation2, byte[] frame, long timestamp2) {
        this.frameWidth = w;
        this.frameHeight = h;
        this.sensorOrientation = sensorOrientation2;
    }

    private void processResults(long timestamp2, List<Classifier.Recognition> results, byte[] originalFrame) {
        new LinkedList();
        this.objects.clear();
        this.objects.addAll(results);
    }

    public synchronized void drawDebug(Canvas canvas) {
    }

    public void setProcessing(boolean processing2) {
        this.processing = processing2;
        this.timestamp = System.currentTimeMillis();
    }

    public void setRectMode(boolean mode) {
        this.rectMode = mode;
    }
}
