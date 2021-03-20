package org.tensorflow.demo.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.p000v4.internal.view.SupportMenu;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.Toast;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.ObjectTracker;

public class MultiBoxTracker {
    private static final int[] COLORS = {-16776961, -65536, -16711936, -256, -16711681, -65281, -1, Color.parseColor("#55FF55"), Color.parseColor("#FFA500"), Color.parseColor("#FF8888"), Color.parseColor("#AAAAFF"), Color.parseColor("#FFFFAA"), Color.parseColor("#55AAAA"), Color.parseColor("#AA33AA"), Color.parseColor("#0D0068")};
    private static final float MARGINAL_CORRELATION = 0.6f;
    private static final float MAX_OVERLAP = 0.2f;
    private static final float MIN_CORRELATION = 0.2f;
    private static final float MIN_SIZE = 16.0f;
    private static final float TEXT_SIZE_DIP = 18.0f;
    private final Queue<Integer> availableColors = new LinkedList();
    private final BorderedText borderedText;
    private final Paint boxPaint = new Paint();
    private Context context;
    private int frameHeight;
    private Matrix frameToCanvasMatrix;
    private int frameWidth;
    private boolean initialized = false;
    private final Logger logger = new Logger();
    public ObjectTracker objectTracker;
    final List<Pair<Float, RectF>> screenRects = new LinkedList();
    private int sensorOrientation;
    private final float textSizePx;
    private final List<TrackedRecognition> trackedObjects = new LinkedList();

    private static class TrackedRecognition {
        int color;
        float detectionConfidence;
        RectF location;
        String title;
        ObjectTracker.TrackedObject trackedObject;

        private TrackedRecognition() {
        }
    }

    public MultiBoxTracker(Context context2) {
        this.context = context2;
        for (int color : COLORS) {
            this.availableColors.add(Integer.valueOf(color));
        }
        this.boxPaint.setColor(SupportMenu.CATEGORY_MASK);
        this.boxPaint.setStyle(Paint.Style.STROKE);
        this.boxPaint.setStrokeWidth(12.0f);
        this.boxPaint.setStrokeCap(Paint.Cap.ROUND);
        this.boxPaint.setStrokeJoin(Paint.Join.ROUND);
        this.boxPaint.setStrokeMiter(100.0f);
        this.textSizePx = TypedValue.applyDimension(1, TEXT_SIZE_DIP, context2.getResources().getDisplayMetrics());
        this.borderedText = new BorderedText(this.textSizePx);
    }

    private Matrix getFrameToCanvasMatrix() {
        return this.frameToCanvasMatrix;
    }

    public synchronized void drawDebug(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(-1);
        textPaint.setTextSize(60.0f);
        Paint boxPaint2 = new Paint();
        boxPaint2.setColor(SupportMenu.CATEGORY_MASK);
        boxPaint2.setAlpha(200);
        boxPaint2.setStyle(Paint.Style.STROKE);
        for (Pair<Float, RectF> detection : this.screenRects) {
            RectF rect = (RectF) detection.second;
            canvas.drawRect(rect, boxPaint2);
            canvas.drawText("" + detection.first, rect.left, rect.top, textPaint);
            this.borderedText.drawText(canvas, rect.centerX(), rect.centerY(), "" + detection.first);
        }
        if (this.objectTracker != null) {
            for (TrackedRecognition recognition : this.trackedObjects) {
                ObjectTracker.TrackedObject trackedObject = recognition.trackedObject;
                RectF trackedPos = trackedObject.getTrackedPositionInPreviewFrame();
                if (getFrameToCanvasMatrix().mapRect(trackedPos)) {
                    Canvas canvas2 = canvas;
                    this.borderedText.drawText(canvas2, trackedPos.right, trackedPos.bottom, String.format("%.2f", new Object[]{Float.valueOf(trackedObject.getCurrentCorrelation())}));
                }
            }
            this.objectTracker.drawDebug(canvas, getFrameToCanvasMatrix());
        }
    }

    public synchronized void trackResults(List<Classifier.Recognition> results, byte[] frame, long timestamp) {
        this.logger.mo6294i("Processing %d results from %d", Integer.valueOf(results.size()), Long.valueOf(timestamp));
        processResults(timestamp, results, frame);
    }

    public synchronized void draw(Canvas canvas) {
        RectF trackedPos;
        String labelString;
        boolean rotated = true;
        synchronized (this) {
            if (this.sensorOrientation % 180 != 90) {
                rotated = false;
            }
            float multiplier = Math.min(((float) canvas.getHeight()) / ((float) (rotated ? this.frameWidth : this.frameHeight)), ((float) canvas.getWidth()) / ((float) (rotated ? this.frameHeight : this.frameWidth)));
            this.frameToCanvasMatrix = ImageUtils.getTransformationMatrix(this.frameWidth, this.frameHeight, (int) (((float) (rotated ? this.frameHeight : this.frameWidth)) * multiplier), (int) (((float) (rotated ? this.frameWidth : this.frameHeight)) * multiplier), this.sensorOrientation, false);
            for (TrackedRecognition recognition : this.trackedObjects) {
                if (this.objectTracker != null) {
                    trackedPos = recognition.trackedObject.getTrackedPositionInPreviewFrame();
                } else {
                    trackedPos = new RectF(recognition.location);
                }
                getFrameToCanvasMatrix().mapRect(trackedPos);
                this.boxPaint.setColor(recognition.color);
                float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
                canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, this.boxPaint);
                if (!TextUtils.isEmpty(recognition.title)) {
                    labelString = String.format("%s %.2f", new Object[]{recognition.title, Float.valueOf(recognition.detectionConfidence)});
                } else {
                    labelString = String.format("%.2f", new Object[]{Float.valueOf(recognition.detectionConfidence)});
                }
                this.borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.bottom, labelString);
            }
        }
    }

    public synchronized void onFrame(int w, int h, int rowStride, int sensorOrientation2, byte[] frame, long timestamp) {
        if (this.objectTracker == null && !this.initialized) {
            ObjectTracker.clearInstance();
            this.logger.mo6294i("Initializing ObjectTracker: %dx%d", Integer.valueOf(w), Integer.valueOf(h));
            this.objectTracker = ObjectTracker.getInstance(w, h, rowStride, true);
            this.frameWidth = w;
            this.frameHeight = h;
            this.sensorOrientation = sensorOrientation2;
            this.initialized = true;
            if (this.objectTracker == null) {
                Toast.makeText(this.context, "Object tracking support not found. See tensorflow/examples/android/README.md for details.", 1).show();
                this.logger.mo6292e("Object tracking support not found. See tensorflow/examples/android/README.md for details.", new Object[0]);
            }
        }
        if (this.objectTracker != null) {
            this.objectTracker.nextFrame(frame, (byte[]) null, timestamp, (float[]) null, true);
            Iterator it = new LinkedList<>(this.trackedObjects).iterator();
            while (it.hasNext()) {
                TrackedRecognition recognition = (TrackedRecognition) it.next();
                ObjectTracker.TrackedObject trackedObject = recognition.trackedObject;
                float correlation = trackedObject.getCurrentCorrelation();
                if (correlation < 0.2f) {
                    this.logger.mo6298v("Removing tracked object %s because NCC is %.2f", trackedObject, Float.valueOf(correlation));
                    trackedObject.stopTracking();
                    this.trackedObjects.remove(recognition);
                    this.availableColors.add(Integer.valueOf(recognition.color));
                }
            }
        }
    }

    private void processResults(long timestamp, List<Classifier.Recognition> results, byte[] originalFrame) {
        List<Pair<Float, Classifier.Recognition>> rectsToTrack = new LinkedList<>();
        this.screenRects.clear();
        Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());
        for (Classifier.Recognition result : results) {
            if (result.getLocation() != null) {
                RectF detectionFrameRect = new RectF(result.getLocation());
                RectF detectionScreenRect = new RectF();
                rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);
                this.logger.mo6298v("Result! Frame: " + result.getLocation() + " mapped to screen:" + detectionScreenRect, new Object[0]);
                this.screenRects.add(new Pair(result.getConfidence(), detectionScreenRect));
                if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {
                    this.logger.mo6300w("Degenerate rectangle! " + detectionFrameRect, new Object[0]);
                } else {
                    rectsToTrack.add(new Pair(result.getConfidence(), result));
                }
            }
        }
        if (rectsToTrack.isEmpty()) {
            this.logger.mo6298v("Nothing to track, aborting.", new Object[0]);
        } else if (this.objectTracker == null) {
            this.trackedObjects.clear();
            for (Pair<Float, Classifier.Recognition> potential : rectsToTrack) {
                TrackedRecognition trackedRecognition = new TrackedRecognition();
                trackedRecognition.detectionConfidence = ((Float) potential.first).floatValue();
                trackedRecognition.location = new RectF(((Classifier.Recognition) potential.second).getLocation());
                trackedRecognition.trackedObject = null;
                trackedRecognition.title = ((Classifier.Recognition) potential.second).getTitle();
                trackedRecognition.color = COLORS[this.trackedObjects.size()];
                this.trackedObjects.add(trackedRecognition);
                if (this.trackedObjects.size() >= COLORS.length) {
                    return;
                }
            }
        } else {
            this.logger.mo6294i("%d rects to track", Integer.valueOf(rectsToTrack.size()));
            for (Pair<Float, Classifier.Recognition> potential2 : rectsToTrack) {
                handleDetection(originalFrame, timestamp, potential2);
            }
        }
    }

    private void handleDetection(byte[] frameCopy, long timestamp, Pair<Float, Classifier.Recognition> potential) {
        ObjectTracker.TrackedObject potentialObject = this.objectTracker.trackObject(((Classifier.Recognition) potential.second).getLocation(), timestamp, frameCopy);
        float potentialCorrelation = potentialObject.getCurrentCorrelation();
        this.logger.mo6298v("Tracked object went from %s to %s with correlation %.2f", potential.second, potentialObject.getTrackedPositionInPreviewFrame(), Float.valueOf(potentialCorrelation));
        if (potentialCorrelation < MARGINAL_CORRELATION) {
            this.logger.mo6298v("Correlation too low to begin tracking %s.", potentialObject);
            potentialObject.stopTracking();
            return;
        }
        LinkedList<TrackedRecognition> linkedList = new LinkedList<>();
        float maxIntersect = 0.0f;
        TrackedRecognition recogToReplace = null;
        for (TrackedRecognition trackedRecognition : this.trackedObjects) {
            RectF a = trackedRecognition.trackedObject.getTrackedPositionInPreviewFrame();
            RectF b = potentialObject.getTrackedPositionInPreviewFrame();
            RectF intersection = new RectF();
            boolean intersects = intersection.setIntersect(a, b);
            float intersectArea = intersection.width() * intersection.height();
            float intersectOverUnion = intersectArea / (((a.width() * a.height()) + (b.width() * b.height())) - intersectArea);
            if (intersects && intersectOverUnion > 0.2f) {
                if (((Float) potential.first).floatValue() >= trackedRecognition.detectionConfidence || trackedRecognition.trackedObject.getCurrentCorrelation() <= MARGINAL_CORRELATION) {
                    linkedList.add(trackedRecognition);
                    if (intersectOverUnion > maxIntersect) {
                        maxIntersect = intersectOverUnion;
                        recogToReplace = trackedRecognition;
                    }
                } else {
                    potentialObject.stopTracking();
                    return;
                }
            }
        }
        if (this.availableColors.isEmpty() && linkedList.isEmpty()) {
            for (TrackedRecognition candidate : this.trackedObjects) {
                if (candidate.detectionConfidence < ((Float) potential.first).floatValue() && (recogToReplace == null || candidate.detectionConfidence < recogToReplace.detectionConfidence)) {
                    recogToReplace = candidate;
                }
            }
            if (recogToReplace != null) {
                this.logger.mo6298v("Found non-intersecting object to remove.", new Object[0]);
                linkedList.add(recogToReplace);
            } else {
                this.logger.mo6298v("No non-intersecting object found to remove", new Object[0]);
            }
        }
        for (TrackedRecognition trackedRecognition2 : linkedList) {
            this.logger.mo6298v("Removing tracked object %s with detection confidence %.2f, correlation %.2f", trackedRecognition2.trackedObject, Float.valueOf(trackedRecognition2.detectionConfidence), Float.valueOf(trackedRecognition2.trackedObject.getCurrentCorrelation()));
            trackedRecognition2.trackedObject.stopTracking();
            this.trackedObjects.remove(trackedRecognition2);
            if (trackedRecognition2 != recogToReplace) {
                this.availableColors.add(Integer.valueOf(trackedRecognition2.color));
            }
        }
        if (recogToReplace != null || !this.availableColors.isEmpty()) {
            this.logger.mo6298v("Tracking object %s (%s) with detection confidence %.2f at position %s", potentialObject, ((Classifier.Recognition) potential.second).getTitle(), potential.first, ((Classifier.Recognition) potential.second).getLocation());
            TrackedRecognition trackedRecognition3 = new TrackedRecognition();
            trackedRecognition3.detectionConfidence = ((Float) potential.first).floatValue();
            trackedRecognition3.trackedObject = potentialObject;
            trackedRecognition3.title = ((Classifier.Recognition) potential.second).getTitle();
            trackedRecognition3.color = recogToReplace != null ? recogToReplace.color : this.availableColors.poll().intValue();
            this.trackedObjects.add(trackedRecognition3);
            return;
        }
        this.logger.mo6292e("No room to track this object, aborting.", new Object[0]);
        potentialObject.stopTracking();
    }
}
