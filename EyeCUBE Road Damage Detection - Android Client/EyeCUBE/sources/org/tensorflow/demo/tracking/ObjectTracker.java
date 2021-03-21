package org.tensorflow.demo.tracking;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.p000v4.internal.view.SupportMenu;
import android.support.p000v4.view.InputDeviceCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.microedition.khronos.opengles.GL10;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.env.Size;

public class ObjectTracker {
    private static final int DOWNSAMPLE_FACTOR = 4;
    private static final boolean DRAW_TEXT = false;
    /* access modifiers changed from: private */
    public static final Logger LOGGER = new Logger();
    private static final int MAX_DEBUG_HISTORY_SIZE = 100;
    private static final int MAX_FRAME_HISTORY_SIZE = 100;
    protected static ObjectTracker instance;
    private static boolean libraryFound;
    protected final boolean alwaysTrack;
    private final Vector<PointF> debugHistory;
    private final byte[] downsampledFrame;
    private long downsampledTimestamp;
    protected final int frameHeight;
    protected final int frameWidth;
    private FrameChange lastKeypoints;
    private long lastTimestamp;
    private final float[] matrixValues = new float[9];
    private long nativeObjectTracker;
    private final int rowStride;
    private final LinkedList<TimestampedDeltas> timestampedDeltas;
    /* access modifiers changed from: private */
    public final Map<String, TrackedObject> trackedObjects;

    protected static native void downsampleImageNative(int i, int i2, int i3, byte[] bArr, int i4, byte[] bArr2);

    private native void initNative(int i, int i2, boolean z);

    /* access modifiers changed from: protected */
    public native void drawNative(int i, int i2, float[] fArr);

    /* access modifiers changed from: protected */
    public native void forgetNative(String str);

    /* access modifiers changed from: protected */
    public native float getCurrentCorrelation(String str);

    /* access modifiers changed from: protected */
    public native void getCurrentPositionNative(long j, float f, float f2, float f3, float f4, float[] fArr);

    /* access modifiers changed from: protected */
    public native float[] getKeypointsNative(boolean z);

    /* access modifiers changed from: protected */
    public native byte[] getKeypointsPacked(float f);

    /* access modifiers changed from: protected */
    public native float getMatchScore(String str);

    /* access modifiers changed from: protected */
    public native String getModelIdNative(String str);

    /* access modifiers changed from: protected */
    public native void getTrackedPositionNative(String str, float[] fArr);

    /* access modifiers changed from: protected */
    public native boolean haveObject(String str);

    /* access modifiers changed from: protected */
    public native boolean isObjectVisible(String str);

    /* access modifiers changed from: protected */
    public native void nextFrameNative(byte[] bArr, byte[] bArr2, long j, float[] fArr);

    /* access modifiers changed from: protected */
    public native void registerNewObjectWithAppearanceNative(String str, float f, float f2, float f3, float f4, byte[] bArr);

    /* access modifiers changed from: protected */
    public native void releaseMemoryNative();

    /* access modifiers changed from: protected */
    public native void setCurrentPositionNative(String str, float f, float f2, float f3, float f4);

    /* access modifiers changed from: protected */
    public native void setPreviousPositionNative(String str, float f, float f2, float f3, float f4, long j);

    static {
        libraryFound = false;
        try {
            System.loadLibrary("tensorflow_demo");
            libraryFound = true;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.mo6292e("libtensorflow_demo.so not found, tracking unavailable", new Object[0]);
        }
    }

    private static class TimestampedDeltas {
        final byte[] deltas;
        final long timestamp;

        public TimestampedDeltas(long timestamp2, byte[] deltas2) {
            this.timestamp = timestamp2;
            this.deltas = deltas2;
        }
    }

    public static class Keypoint {
        public final float score;
        public final int type;

        /* renamed from: x */
        public final float f24x;

        /* renamed from: y */
        public final float f25y;

        public Keypoint(float x, float y) {
            this.f24x = x;
            this.f25y = y;
            this.score = 0.0f;
            this.type = -1;
        }

        public Keypoint(float x, float y, float score2, int type2) {
            this.f24x = x;
            this.f25y = y;
            this.score = score2;
            this.type = type2;
        }

        /* access modifiers changed from: package-private */
        public Keypoint delta(Keypoint other) {
            return new Keypoint(this.f24x - other.f24x, this.f25y - other.f25y);
        }
    }

    public static class PointChange {
        public final Keypoint keypointA;
        public final Keypoint keypointB;
        Keypoint pointDelta;
        /* access modifiers changed from: private */
        public final boolean wasFound;

        public PointChange(float x1, float y1, float x2, float y2, float score, int type, boolean wasFound2) {
            this.wasFound = wasFound2;
            this.keypointA = new Keypoint(x1, y1, score, type);
            this.keypointB = new Keypoint(x2, y2);
        }

        public Keypoint getDelta() {
            if (this.pointDelta == null) {
                this.pointDelta = this.keypointB.delta(this.keypointA);
            }
            return this.pointDelta;
        }
    }

    public static class FrameChange {
        public static final int KEYPOINT_STEP = 7;
        /* access modifiers changed from: private */
        public final float maxScore;
        /* access modifiers changed from: private */
        public final float minScore;
        public final Vector<PointChange> pointDeltas;

        public FrameChange(float[] framePoints) {
            float minScore2 = 100.0f;
            float maxScore2 = -100.0f;
            this.pointDeltas = new Vector<>(framePoints.length / 7);
            for (int i = 0; i < framePoints.length; i += 7) {
                float x1 = framePoints[i + 0] * 4.0f;
                float y1 = framePoints[i + 1] * 4.0f;
                boolean wasFound = framePoints[i + 2] > 0.0f;
                float x2 = framePoints[i + 3] * 4.0f;
                float y2 = framePoints[i + 4] * 4.0f;
                float score = framePoints[i + 5];
                minScore2 = Math.min(minScore2, score);
                maxScore2 = Math.max(maxScore2, score);
                this.pointDeltas.add(new PointChange(x1, y1, x2, y2, score, (int) framePoints[i + 6], wasFound));
            }
            this.minScore = minScore2;
            this.maxScore = maxScore2;
        }
    }

    public static synchronized ObjectTracker getInstance(int frameWidth2, int frameHeight2, int rowStride2, boolean alwaysTrack2) {
        ObjectTracker objectTracker;
        synchronized (ObjectTracker.class) {
            if (!libraryFound) {
                LOGGER.mo6292e("Native object tracking support not found. See tensorflow/examples/android/README.md for details.", new Object[0]);
                objectTracker = null;
            } else if (instance == null) {
                instance = new ObjectTracker(frameWidth2, frameHeight2, rowStride2, alwaysTrack2);
                instance.init();
                objectTracker = instance;
            } else {
                throw new RuntimeException("Tried to create a new objectracker before releasing the old one!");
            }
        }
        return objectTracker;
    }

    public static synchronized void clearInstance() {
        synchronized (ObjectTracker.class) {
            if (instance != null) {
                instance.release();
            }
        }
    }

    protected ObjectTracker(int frameWidth2, int frameHeight2, int rowStride2, boolean alwaysTrack2) {
        this.frameWidth = frameWidth2;
        this.frameHeight = frameHeight2;
        this.rowStride = rowStride2;
        this.alwaysTrack = alwaysTrack2;
        this.timestampedDeltas = new LinkedList<>();
        this.trackedObjects = new HashMap();
        this.debugHistory = new Vector<>(100);
        this.downsampledFrame = new byte[(((((frameWidth2 + 4) - 1) / 4) * ((frameWidth2 + 4) - 1)) / 4)];
    }

    /* access modifiers changed from: protected */
    public void init() {
        initNative(this.frameWidth / 4, this.frameHeight / 4, this.alwaysTrack);
    }

    public synchronized void drawOverlay(GL10 gl, Size cameraViewSize, Matrix matrix) {
        Matrix tempMatrix = new Matrix(matrix);
        tempMatrix.preScale(4.0f, 4.0f);
        tempMatrix.getValues(this.matrixValues);
        drawNative(cameraViewSize.width, cameraViewSize.height, this.matrixValues);
    }

    public synchronized void nextFrame(byte[] frameData, byte[] uvData, long timestamp, float[] transformationMatrix, boolean updateDebugInfo) {
        if (this.downsampledTimestamp != timestamp) {
            downsampleImageNative(this.frameWidth, this.frameHeight, this.rowStride, frameData, 4, this.downsampledFrame);
            this.downsampledTimestamp = timestamp;
        }
        nextFrameNative(this.downsampledFrame, uvData, timestamp, transformationMatrix);
        this.timestampedDeltas.add(new TimestampedDeltas(timestamp, getKeypointsPacked(4.0f)));
        while (this.timestampedDeltas.size() > 100) {
            this.timestampedDeltas.removeFirst();
        }
        for (TrackedObject trackedObject : this.trackedObjects.values()) {
            trackedObject.updateTrackedPosition();
        }
        if (updateDebugInfo) {
            updateDebugHistory();
        }
        this.lastTimestamp = timestamp;
    }

    public synchronized void release() {
        releaseMemoryNative();
        synchronized (ObjectTracker.class) {
            instance = null;
        }
    }

    private void drawHistoryDebug(Canvas canvas) {
        drawHistoryPoint(canvas, (float) (((this.frameWidth / 2) * 4) / 2), (float) (((this.frameHeight / 2) * 4) / 2));
    }

    private void drawHistoryPoint(Canvas canvas, float startX, float startY) {
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setTypeface(Typeface.SERIF);
        p.setColor(SupportMenu.CATEGORY_MASK);
        p.setStrokeWidth(2.0f);
        p.setColor(-16711936);
        canvas.drawCircle(startX, startY, 3.0f, p);
        p.setColor(SupportMenu.CATEGORY_MASK);
        synchronized (this.debugHistory) {
            int numPoints = this.debugHistory.size();
            float lastX = startX;
            float lastY = startY;
            for (int keypointNum = 0; keypointNum < numPoints; keypointNum++) {
                PointF delta = this.debugHistory.get((numPoints - keypointNum) - 1);
                float newX = lastX + delta.x;
                float newY = lastY + delta.y;
                canvas.drawLine(lastX, lastY, newX, newY, p);
                lastX = newX;
                lastY = newY;
            }
        }
    }

    private static int floatToChar(float value) {
        return Math.max(0, Math.min((int) (255.999f * value), 255));
    }

    private void drawKeypointsDebug(Canvas canvas) {
        Paint p = new Paint();
        if (this.lastKeypoints != null) {
            float minScore = this.lastKeypoints.minScore;
            float maxScore = this.lastKeypoints.maxScore;
            Iterator<PointChange> it = this.lastKeypoints.pointDeltas.iterator();
            while (it.hasNext()) {
                PointChange keypoint = it.next();
                if (keypoint.wasFound) {
                    int r = floatToChar((keypoint.keypointA.score - minScore) / (maxScore - minScore));
                    p.setColor(-16777216 | (r << 16) | floatToChar(1.0f - ((keypoint.keypointA.score - minScore) / (maxScore - minScore))));
                    float[] screenPoints = {keypoint.keypointA.f24x, keypoint.keypointA.f25y, keypoint.keypointB.f24x, keypoint.keypointB.f25y};
                    canvas.drawRect(screenPoints[2] - 3.0f, screenPoints[3] - 3.0f, 3.0f + screenPoints[2], 3.0f + screenPoints[3], p);
                    p.setColor(-16711681);
                    canvas.drawLine(screenPoints[2], screenPoints[3], screenPoints[0], screenPoints[1], p);
                } else {
                    p.setColor(InputDeviceCompat.SOURCE_ANY);
                    float[] screenPoint = {keypoint.keypointA.f24x, keypoint.keypointA.f25y};
                    canvas.drawCircle(screenPoint[0], screenPoint[1], 5.0f, p);
                }
            }
        }
    }

    private synchronized PointF getAccumulatedDelta(long timestamp, float positionX, float positionY, float radius) {
        RectF currPosition;
        currPosition = getCurrentPosition(timestamp, new RectF(positionX - radius, positionY - radius, positionX + radius, positionY + radius));
        return new PointF(currPosition.centerX() - positionX, currPosition.centerY() - positionY);
    }

    private synchronized RectF getCurrentPosition(long timestamp, RectF oldPosition) {
        float[] delta;
        RectF downscaledFrameRect = downscaleRect(oldPosition);
        delta = new float[4];
        getCurrentPositionNative(timestamp, downscaledFrameRect.left, downscaledFrameRect.top, downscaledFrameRect.right, downscaledFrameRect.bottom, delta);
        return upscaleRect(new RectF(delta[0], delta[1], delta[2], delta[3]));
    }

    private void updateDebugHistory() {
        this.lastKeypoints = new FrameChange(getKeypointsNative(false));
        if (this.lastTimestamp != 0) {
            PointF delta = getAccumulatedDelta(this.lastTimestamp, (float) ((this.frameWidth / 2) / 4), (float) ((this.frameHeight / 2) / 4), 100.0f);
            synchronized (this.debugHistory) {
                this.debugHistory.add(delta);
                while (this.debugHistory.size() > 100) {
                    this.debugHistory.remove(0);
                }
            }
        }
    }

    public synchronized void drawDebug(Canvas canvas, Matrix frameToCanvas) {
        canvas.save();
        canvas.setMatrix(frameToCanvas);
        drawHistoryDebug(canvas);
        drawKeypointsDebug(canvas);
        canvas.restore();
    }

    public Vector<String> getDebugText() {
        Vector<String> lines = new Vector<>();
        if (this.lastKeypoints != null) {
            lines.add("Num keypoints " + this.lastKeypoints.pointDeltas.size());
            lines.add("Min score: " + this.lastKeypoints.minScore);
            lines.add("Max score: " + this.lastKeypoints.maxScore);
        }
        return lines;
    }

    public synchronized List<byte[]> pollAccumulatedFlowData(long endFrameTime) {
        List<byte[]> frameDeltas;
        frameDeltas = new ArrayList<>();
        while (this.timestampedDeltas.size() > 0) {
            TimestampedDeltas currentDeltas = this.timestampedDeltas.peek();
            if (currentDeltas.timestamp > endFrameTime) {
                break;
            }
            frameDeltas.add(currentDeltas.deltas);
            this.timestampedDeltas.removeFirst();
        }
        return frameDeltas;
    }

    /* access modifiers changed from: private */
    public RectF downscaleRect(RectF fullFrameRect) {
        return new RectF(fullFrameRect.left / 4.0f, fullFrameRect.top / 4.0f, fullFrameRect.right / 4.0f, fullFrameRect.bottom / 4.0f);
    }

    /* access modifiers changed from: private */
    public RectF upscaleRect(RectF downsampledFrameRect) {
        return new RectF(downsampledFrameRect.left * 4.0f, downsampledFrameRect.top * 4.0f, downsampledFrameRect.right * 4.0f, downsampledFrameRect.bottom * 4.0f);
    }

    public class TrackedObject {

        /* renamed from: id */
        private final String f26id = Integer.toString(hashCode());
        private boolean isDead = false;
        private long lastExternalPositionTime;
        private RectF lastTrackedPosition;
        private boolean visibleInLastFrame;

        TrackedObject(RectF position, long timestamp, byte[] data) {
            this.lastExternalPositionTime = timestamp;
            synchronized (ObjectTracker.this) {
                registerInitialAppearance(position, data);
                setPreviousPosition(position, timestamp);
                ObjectTracker.this.trackedObjects.put(this.f26id, this);
            }
        }

        public void stopTracking() {
            checkValidObject();
            synchronized (ObjectTracker.this) {
                this.isDead = true;
                ObjectTracker.this.forgetNative(this.f26id);
                ObjectTracker.this.trackedObjects.remove(this.f26id);
            }
        }

        public float getCurrentCorrelation() {
            checkValidObject();
            return ObjectTracker.this.getCurrentCorrelation(this.f26id);
        }

        /* access modifiers changed from: package-private */
        public void registerInitialAppearance(RectF position, byte[] data) {
            RectF externalPosition = ObjectTracker.this.downscaleRect(position);
            ObjectTracker.this.registerNewObjectWithAppearanceNative(this.f26id, externalPosition.left, externalPosition.top, externalPosition.right, externalPosition.bottom, data);
        }

        /* access modifiers changed from: package-private */
        public synchronized void setPreviousPosition(RectF position, long timestamp) {
            checkValidObject();
            synchronized (ObjectTracker.this) {
                if (this.lastExternalPositionTime > timestamp) {
                    ObjectTracker.LOGGER.mo6300w("Tried to use older position time!", new Object[0]);
                } else {
                    RectF externalPosition = ObjectTracker.this.downscaleRect(position);
                    this.lastExternalPositionTime = timestamp;
                    ObjectTracker.this.setPreviousPositionNative(this.f26id, externalPosition.left, externalPosition.top, externalPosition.right, externalPosition.bottom, this.lastExternalPositionTime);
                    updateTrackedPosition();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setCurrentPosition(RectF position) {
            checkValidObject();
            RectF downsampledPosition = ObjectTracker.this.downscaleRect(position);
            synchronized (ObjectTracker.this) {
                ObjectTracker.this.setCurrentPositionNative(this.f26id, downsampledPosition.left, downsampledPosition.top, downsampledPosition.right, downsampledPosition.bottom);
            }
        }

        /* access modifiers changed from: private */
        public synchronized void updateTrackedPosition() {
            checkValidObject();
            float[] delta = new float[4];
            ObjectTracker.this.getTrackedPositionNative(this.f26id, delta);
            this.lastTrackedPosition = new RectF(delta[0], delta[1], delta[2], delta[3]);
            this.visibleInLastFrame = ObjectTracker.this.isObjectVisible(this.f26id);
        }

        public synchronized RectF getTrackedPositionInPreviewFrame() {
            RectF access$700;
            checkValidObject();
            if (this.lastTrackedPosition == null) {
                access$700 = null;
            } else {
                access$700 = ObjectTracker.this.upscaleRect(this.lastTrackedPosition);
            }
            return access$700;
        }

        /* access modifiers changed from: package-private */
        public synchronized long getLastExternalPositionTime() {
            return this.lastExternalPositionTime;
        }

        public synchronized boolean visibleInLastPreviewFrame() {
            return this.visibleInLastFrame;
        }

        private void checkValidObject() {
            if (this.isDead) {
                throw new RuntimeException("TrackedObject already removed from tracking!");
            } else if (ObjectTracker.this != ObjectTracker.instance) {
                throw new RuntimeException("TrackedObject created with another ObjectTracker!");
            }
        }
    }

    public synchronized TrackedObject trackObject(RectF position, long timestamp, byte[] frameData) {
        if (this.downsampledTimestamp != timestamp) {
            downsampleImageNative(this.frameWidth, this.frameHeight, this.rowStride, frameData, 4, this.downsampledFrame);
            this.downsampledTimestamp = timestamp;
        }
        return new TrackedObject(position, timestamp, this.downsampledFrame);
    }

    public synchronized TrackedObject trackObject(RectF position, byte[] frameData) {
        return new TrackedObject(position, this.lastTimestamp, frameData);
    }
}
