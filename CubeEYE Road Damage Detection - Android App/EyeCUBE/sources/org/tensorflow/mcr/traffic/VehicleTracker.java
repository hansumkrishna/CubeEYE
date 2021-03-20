package org.tensorflow.mcr.traffic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.p000v4.internal.view.SupportMenu;
import android.support.p000v4.view.InputDeviceCompat;
import android.util.Pair;
import android.util.TypedValue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.ObjectTracker;

public class VehicleTracker {
    private static final float MARGINAL_CORRELATION = 0.3f;
    private static final int MAX_NUM_VEHICLES = 30;
    private static final float MAX_OVERLAP = 0.2f;
    private static final float MAX_OVERLAP_TRACK = 0.4f;
    private static final float MAX_SIZE = 160.0f;
    private static final float MIN_SIZE = 30.0f;
    private static final float TEXT_SIZE_DIP = 18.0f;
    private int GUIDE_LINE_MARGIN = 50;
    private int GUIDE_LINE_POSITION = 100;
    private final BorderedText borderedText;
    private final Paint boxPaint = new Paint();
    private Context context;
    private boolean detectingMode = false;
    private int frameHeight;
    private Matrix frameToCanvasMatrix;
    private int frameWidth;
    private final Paint guidePaint1 = new Paint();
    private final Paint guidePaint2 = new Paint();
    private RectF guideRect1 = null;
    private RectF guideRect2 = null;
    private boolean initialized = false;
    private final Logger logger = new Logger();
    private final Paint modePaint = new Paint();
    private long numVehicles1 = 0;
    private long numVehicles2 = 0;
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

    public VehicleTracker(Context context2) {
        this.context = context2;
        this.boxPaint.setColor(SupportMenu.CATEGORY_MASK);
        this.boxPaint.setStyle(Paint.Style.STROKE);
        this.boxPaint.setStrokeWidth(5.0f);
        this.boxPaint.setStrokeCap(Paint.Cap.ROUND);
        this.boxPaint.setStrokeJoin(Paint.Join.ROUND);
        this.boxPaint.setStrokeMiter(100.0f);
        this.guidePaint1.setStyle(Paint.Style.STROKE);
        this.guidePaint1.setStrokeWidth(15.0f);
        this.guidePaint1.setColor(InputDeviceCompat.SOURCE_ANY);
        this.guidePaint2.setStyle(Paint.Style.STROKE);
        this.guidePaint2.setStrokeWidth(15.0f);
        this.guidePaint2.setColor(-16776961);
        this.modePaint.setColor(SupportMenu.CATEGORY_MASK);
        this.modePaint.setTextSize(80.0f);
        this.textSizePx = TypedValue.applyDimension(1, TEXT_SIZE_DIP, context2.getResources().getDisplayMetrics());
        this.borderedText = new BorderedText(this.textSizePx);
    }

    private Matrix getFrameToCanvasMatrix() {
        return this.frameToCanvasMatrix;
    }

    public synchronized void drawDebug(Canvas canvas) {
        if (this.objectTracker != null) {
            if (this.guideRect1 != null) {
                RectF rect = new RectF(this.guideRect1);
                getFrameToCanvasMatrix().mapRect(rect);
                canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, this.guidePaint1);
                canvas.drawLine(rect.right, rect.top, rect.left, rect.top, this.guidePaint1);
            }
            for (TrackedRecognition recognition : this.trackedObjects) {
                ObjectTracker.TrackedObject trackedObject = recognition.trackedObject;
                RectF trackedPos = trackedObject.getTrackedPositionInPreviewFrame();
                if (getFrameToCanvasMatrix().mapRect(trackedPos)) {
                    this.borderedText.drawText(canvas, trackedPos.right, trackedPos.bottom, String.format("%.2f", new Object[]{Float.valueOf(trackedObject.getCurrentCorrelation())}));
                }
            }
            this.objectTracker.drawDebug(canvas, getFrameToCanvasMatrix());
        }
    }

    public synchronized void trackResults(List<Classifier.Recognition> results, byte[] frame, long timestamp) {
        processResults(timestamp, results, frame);
    }

    public synchronized void draw(Canvas canvas) {
        RectF trackedPos;
        boolean rotated = true;
        synchronized (this) {
            if (this.sensorOrientation % 180 != 90) {
                rotated = false;
            }
            float multiplier = Math.min(((float) canvas.getHeight()) / ((float) (rotated ? this.frameWidth : this.frameHeight)), ((float) canvas.getWidth()) / ((float) (rotated ? this.frameHeight : this.frameWidth)));
            this.frameToCanvasMatrix = ImageUtils.getTransformationMatrix(this.frameWidth, this.frameHeight, (int) (((float) (rotated ? this.frameHeight : this.frameWidth)) * multiplier), (int) (((float) (rotated ? this.frameWidth : this.frameHeight)) * multiplier), this.sensorOrientation, false);
            if (this.guideRect2 != null) {
                RectF rect = new RectF(this.guideRect2);
                getFrameToCanvasMatrix().mapRect(rect);
                canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, this.guidePaint2);
                canvas.drawLine(rect.right, rect.top, rect.left, rect.top, this.guidePaint2);
            }
            for (TrackedRecognition recognition : this.trackedObjects) {
                if (this.objectTracker != null) {
                    trackedPos = recognition.trackedObject.getTrackedPositionInPreviewFrame();
                } else {
                    trackedPos = new RectF(recognition.location);
                }
                getFrameToCanvasMatrix().mapRect(trackedPos);
                float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
                canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, this.boxPaint);
            }
            if (this.detectingMode) {
                canvas.drawText(String.format("Number of Vehicles: %d %d", new Object[]{Long.valueOf(this.numVehicles1), Long.valueOf(this.numVehicles2)}), 100.0f, 200.0f, this.modePaint);
            }
        }
    }

    private boolean countUp(float pos) {
        if (withinRect(pos, this.guideRect2)) {
            return false;
        }
        if (pos < this.guideRect2.left) {
            this.numVehicles1++;
        } else if (pos > this.guideRect2.right) {
            this.numVehicles2++;
        }
        return true;
    }

    private boolean withinRect(float pos, RectF rect) {
        if (rect.left >= pos || rect.right <= pos) {
            return false;
        }
        return true;
    }

    public synchronized void onFrame(int w, int h, int rowStride, int sensorOrientation2, byte[] frame, long timestamp) {
        if (this.objectTracker == null && !this.initialized) {
            ObjectTracker.clearInstance();
            this.objectTracker = ObjectTracker.getInstance(w, h, rowStride, true);
            this.frameWidth = w;
            this.frameHeight = h;
            this.sensorOrientation = sensorOrientation2;
            this.initialized = true;
            this.guideRect1 = new RectF((float) ((this.frameWidth - this.frameHeight) + this.GUIDE_LINE_POSITION), 0.0f, (float) (this.frameWidth - this.GUIDE_LINE_POSITION), (float) this.frameHeight);
            this.guideRect2 = new RectF(this.guideRect1);
            this.guideRect2.left -= (float) this.GUIDE_LINE_MARGIN;
            this.guideRect2.right += (float) this.GUIDE_LINE_MARGIN;
        }
        if (this.objectTracker != null) {
            this.objectTracker.nextFrame(frame, (byte[]) null, timestamp, (float[]) null, true);
            LinkedList<TrackedRecognition> copyList = new LinkedList<>(this.trackedObjects);
            HashSet<TrackedRecognition> removeSet = new HashSet<>();
            for (int i = 0; i < copyList.size(); i++) {
                for (int j = i + 1; j < copyList.size(); j++) {
                    TrackedRecognition recognition1 = copyList.get(i);
                    TrackedRecognition recognition2 = copyList.get(j);
                    RectF a = recognition1.trackedObject.getTrackedPositionInPreviewFrame();
                    RectF b = recognition2.trackedObject.getTrackedPositionInPreviewFrame();
                    RectF intersection = new RectF();
                    if (intersection.setIntersect(a, b)) {
                        float intersectArea = intersection.width() * intersection.height();
                        float aArea = a.width() * a.height();
                        float bArea = b.width() * b.height();
                        if (aArea > bArea) {
                            if (intersectArea / bArea > MAX_OVERLAP_TRACK) {
                                removeSet.add(recognition2);
                            }
                        } else if (intersectArea / aArea > MAX_OVERLAP_TRACK) {
                            removeSet.add(recognition1);
                        }
                    }
                }
            }
            Iterator it = copyList.iterator();
            while (it.hasNext()) {
                TrackedRecognition recognition = (TrackedRecognition) it.next();
                RectF a2 = recognition.trackedObject.getTrackedPositionInPreviewFrame();
                if (!removeSet.contains(recognition)) {
                    if (countUp(a2.centerX()) || a2.width() < MIN_SIZE || a2.width() > MAX_SIZE || a2.height() < MIN_SIZE || a2.height() > MAX_SIZE) {
                        removeSet.add(recognition);
                    }
                }
            }
            Iterator<TrackedRecognition> it2 = removeSet.iterator();
            while (it2.hasNext()) {
                TrackedRecognition recognition3 = it2.next();
                recognition3.trackedObject.stopTracking();
                this.trackedObjects.remove(recognition3);
            }
        }
    }

    private void processResults(long timestamp, List<Classifier.Recognition> results, byte[] originalFrame) {
        List<Pair<Float, Classifier.Recognition>> rectsToTrack = new LinkedList<>();
        this.screenRects.clear();
        Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());
        for (Classifier.Recognition result : results) {
            if (result.getLocation() != null && withinRect(result.getLocation().left, this.guideRect1) && withinRect(result.getLocation().right, this.guideRect1)) {
                RectF detectionFrameRect = new RectF(result.getLocation());
                RectF detectionScreenRect = new RectF();
                rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);
                this.screenRects.add(new Pair(result.getConfidence(), detectionScreenRect));
                if (detectionFrameRect.width() >= MIN_SIZE && detectionFrameRect.height() >= MIN_SIZE && detectionFrameRect.width() <= MAX_SIZE && detectionFrameRect.height() <= MAX_SIZE) {
                    rectsToTrack.add(new Pair(result.getConfidence(), result));
                }
            }
        }
        if (!rectsToTrack.isEmpty()) {
            if (this.objectTracker == null) {
                this.trackedObjects.clear();
                for (Pair<Float, Classifier.Recognition> potential : rectsToTrack) {
                    TrackedRecognition trackedRecognition = new TrackedRecognition();
                    trackedRecognition.detectionConfidence = ((Float) potential.first).floatValue();
                    trackedRecognition.location = new RectF(((Classifier.Recognition) potential.second).getLocation());
                    trackedRecognition.trackedObject = null;
                    trackedRecognition.title = ((Classifier.Recognition) potential.second).getTitle();
                    trackedRecognition.color = -16711936;
                    this.trackedObjects.add(trackedRecognition);
                    if (this.trackedObjects.size() >= 30) {
                        return;
                    }
                }
                return;
            }
            for (Pair<Float, Classifier.Recognition> potential2 : rectsToTrack) {
                handleDetection(originalFrame, timestamp, potential2);
            }
        }
    }

    private void handleDetection(byte[] frameCopy, long timestamp, Pair<Float, Classifier.Recognition> potential) {
        ObjectTracker.TrackedObject potentialObject = this.objectTracker.trackObject(((Classifier.Recognition) potential.second).getLocation(), timestamp, frameCopy);
        if (potentialObject.getCurrentCorrelation() < MARGINAL_CORRELATION) {
            potentialObject.stopTracking();
            return;
        }
        List<TrackedRecognition> removeList = new LinkedList<>();
        for (TrackedRecognition trackedRecognition : this.trackedObjects) {
            RectF a = trackedRecognition.trackedObject.getTrackedPositionInPreviewFrame();
            RectF b = potentialObject.getTrackedPositionInPreviewFrame();
            RectF intersection = new RectF();
            boolean intersects = intersection.setIntersect(a, b);
            float intersectArea = intersection.width() * intersection.height();
            float intersectOverUnion = intersectArea / (((a.width() * a.height()) + (b.width() * b.height())) - intersectArea);
            if (intersects && intersectOverUnion > MAX_OVERLAP) {
                if (((Float) potential.first).floatValue() >= trackedRecognition.detectionConfidence || trackedRecognition.trackedObject.getCurrentCorrelation() <= MARGINAL_CORRELATION) {
                    removeList.add(trackedRecognition);
                } else {
                    potentialObject.stopTracking();
                    return;
                }
            }
        }
        for (TrackedRecognition trackedRecognition2 : removeList) {
            trackedRecognition2.trackedObject.stopTracking();
            this.trackedObjects.remove(trackedRecognition2);
        }
        if (this.trackedObjects.size() > 30) {
            potentialObject.stopTracking();
            return;
        }
        TrackedRecognition trackedRecognition3 = new TrackedRecognition();
        trackedRecognition3.detectionConfidence = ((Float) potential.first).floatValue();
        trackedRecognition3.trackedObject = potentialObject;
        trackedRecognition3.title = ((Classifier.Recognition) potential.second).getTitle();
        this.trackedObjects.add(trackedRecognition3);
    }

    public synchronized void detecting(boolean detectingMode2) {
        this.detectingMode = detectingMode2;
        this.numVehicles2 = 0;
        this.numVehicles1 = 0;
        Iterator it = new LinkedList<>(this.trackedObjects).iterator();
        while (it.hasNext()) {
            TrackedRecognition recognition = (TrackedRecognition) it.next();
            recognition.trackedObject.stopTracking();
            this.trackedObjects.remove(recognition);
        }
    }
}
