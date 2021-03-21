package org.tensorflow.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader;
import android.os.SystemClock;
import android.support.p000v4.internal.view.SupportMenu;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;
import org.ateam.eyecube.mcr.R;

public class DetectorActivity extends CameraActivity implements ImageReader.OnImageAvailableListener {
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    /* access modifiers changed from: private */
    public static final Logger LOGGER = new Logger();
    private static final boolean MAINTAIN_ASPECT = true;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128.0f;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final int MB_INPUT_SIZE = 224;
    private static final String MB_LOCATION_FILE = "file:///android_asset/multibox_location_priors.txt";
    private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.4f;
    private static final float MINIMUM_CONFIDENCE_YOLO = 0.25f;
    /* access modifiers changed from: private */
    public static final DetectorMode MODE = DetectorMode.TF_OD_API;
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10.0f;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
    private static final int YOLO_BLOCK_SIZE = 32;
    private static final String YOLO_INPUT_NAME = "input";
    private static final int YOLO_INPUT_SIZE = 416;
    private static final String YOLO_MODEL_FILE = "file:///android_asset/graph-tiny-yolo-voc.pb";
    private static final String YOLO_OUTPUT_NAMES = "output";
    /* access modifiers changed from: private */
    public BorderedText borderedText;
    /* access modifiers changed from: private */
    public boolean computingDetection = false;
    /* access modifiers changed from: private */
    public Bitmap cropCopyBitmap = null;
    /* access modifiers changed from: private */
    public Matrix cropToFrameTransform;
    /* access modifiers changed from: private */
    public Bitmap croppedBitmap = null;
    /* access modifiers changed from: private */
    public Classifier detector;
    private Matrix frameToCropTransform;
    /* access modifiers changed from: private */
    public long lastProcessingTimeMs;
    /* access modifiers changed from: private */
    public byte[] luminanceCopy;
    private Bitmap rgbFrameBitmap = null;
    /* access modifiers changed from: private */
    public Integer sensorOrientation;
    private long timestamp = 0;
    /* access modifiers changed from: private */
    public MultiBoxTracker tracker;
    OverlayView trackingOverlay;

    private enum DetectorMode {
        TF_OD_API,
        MULTIBOX,
        YOLO
    }

    public void onPreviewSizeChosen(Size size, int rotation) {
        this.borderedText = new BorderedText(TypedValue.applyDimension(1, TEXT_SIZE_DIP, getResources().getDisplayMetrics()));
        this.borderedText.setTypeface(Typeface.MONOSPACE);
        this.tracker = new MultiBoxTracker(this);
        int cropSize = TF_OD_API_INPUT_SIZE;
        if (MODE == DetectorMode.YOLO) {
            this.detector = TensorFlowYoloDetector.create(getAssets(), YOLO_MODEL_FILE, YOLO_INPUT_SIZE, YOLO_INPUT_NAME, YOLO_OUTPUT_NAMES, 32);
            cropSize = YOLO_INPUT_SIZE;
        } else if (MODE == DetectorMode.MULTIBOX) {
            this.detector = TensorFlowMultiBoxDetector.create(getAssets(), MB_MODEL_FILE, MB_LOCATION_FILE, 128, MB_IMAGE_STD, MB_INPUT_NAME, MB_OUTPUT_LOCATIONS_NAME, MB_OUTPUT_SCORES_NAME);
            cropSize = MB_INPUT_SIZE;
        } else {
            try {
                this.detector = TensorFlowObjectDetectionAPIModel.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
                cropSize = TF_OD_API_INPUT_SIZE;
            } catch (IOException e) {
                LOGGER.mo6292e("Exception initializing classifier!", e);
                Toast.makeText(getApplicationContext(), "Classifier could not be initialized", 0).show();
                finish();
            }
        }
        this.previewWidth = size.getWidth();
        this.previewHeight = size.getHeight();
        this.sensorOrientation = Integer.valueOf(rotation - getScreenOrientation());
        LOGGER.mo6294i("Camera orientation relative to screen canvas: %d", this.sensorOrientation);
        LOGGER.mo6294i("Initializing at size %dx%d", Integer.valueOf(this.previewWidth), Integer.valueOf(this.previewHeight));
        this.rgbFrameBitmap = Bitmap.createBitmap(this.previewWidth, this.previewHeight, Bitmap.Config.ARGB_8888);
        this.croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
        this.frameToCropTransform = ImageUtils.getTransformationMatrix(this.previewWidth, this.previewHeight, cropSize, cropSize, this.sensorOrientation.intValue(), MAINTAIN_ASPECT);
        this.cropToFrameTransform = new Matrix();
        this.frameToCropTransform.invert(this.cropToFrameTransform);
        this.trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        this.trackingOverlay.addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                DetectorActivity.this.tracker.draw(canvas);
                if (DetectorActivity.this.isDebug()) {
                    DetectorActivity.this.tracker.drawDebug(canvas);
                }
            }
        });
        addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                Bitmap copy;
                if (DetectorActivity.this.isDebug() && (copy = DetectorActivity.this.cropCopyBitmap) != null) {
                    canvas.drawColor(Color.argb(100, 0, 0, 0));
                    Matrix matrix = new Matrix();
                    matrix.postScale(2.0f, 2.0f);
                    matrix.postTranslate(((float) canvas.getWidth()) - (((float) copy.getWidth()) * 2.0f), ((float) canvas.getHeight()) - (((float) copy.getHeight()) * 2.0f));
                    canvas.drawBitmap(copy, matrix, new Paint());
                    Vector<String> lines = new Vector<>();
                    lines.add("Frame: " + DetectorActivity.this.previewWidth + "x" + DetectorActivity.this.previewHeight);
                    lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                    lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                    lines.add("Rotation: " + DetectorActivity.this.sensorOrientation);
                    lines.add("Inference time: " + DetectorActivity.this.lastProcessingTimeMs + "ms");
                    DetectorActivity.this.borderedText.drawLines(canvas, DetectorActivity.TEXT_SIZE_DIP, (float) (canvas.getHeight() - 10), lines);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void processImage() {
        this.timestamp++;
        long currTimestamp = this.timestamp;
        byte[] originalLuminance = getLuminance();
        this.tracker.onFrame(this.previewWidth, this.previewHeight, getLuminanceStride(), this.sensorOrientation.intValue(), originalLuminance, this.timestamp);
        this.trackingOverlay.postInvalidate();
        if (this.computingDetection) {
            readyForNextImage();
            return;
        }
        this.computingDetection = MAINTAIN_ASPECT;
        LOGGER.mo6294i("Preparing image " + currTimestamp + " for detection in bg thread.", new Object[0]);
        this.rgbFrameBitmap.setPixels(getRgbBytes(), 0, this.previewWidth, 0, 0, this.previewWidth, this.previewHeight);
        if (this.luminanceCopy == null) {
            this.luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, this.luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();
        new Canvas(this.croppedBitmap).drawBitmap(this.rgbFrameBitmap, this.frameToCropTransform, (Paint) null);
        final long j = currTimestamp;
        runInBackground(new Runnable() {
            public void run() {
                DetectorActivity.LOGGER.mo6294i("Running detection on image " + j, new Object[0]);
                long startTime = SystemClock.uptimeMillis();
                List<Classifier.Recognition> results = DetectorActivity.this.detector.recognizeImage(DetectorActivity.this.croppedBitmap);
                long unused = DetectorActivity.this.lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                Bitmap unused2 = DetectorActivity.this.cropCopyBitmap = Bitmap.createBitmap(DetectorActivity.this.croppedBitmap);
                Canvas canvas = new Canvas(DetectorActivity.this.cropCopyBitmap);
                Paint paint = new Paint();
                paint.setColor(SupportMenu.CATEGORY_MASK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);
                float minimumConfidence = DetectorActivity.MINIMUM_CONFIDENCE_TF_OD_API;
                switch (C02814.$SwitchMap$org$tensorflow$demo$DetectorActivity$DetectorMode[DetectorActivity.MODE.ordinal()]) {
                    case 1:
                        minimumConfidence = DetectorActivity.MINIMUM_CONFIDENCE_TF_OD_API;
                        break;
                    case 2:
                        minimumConfidence = DetectorActivity.MINIMUM_CONFIDENCE_MULTIBOX;
                        break;
                    case 3:
                        minimumConfidence = DetectorActivity.MINIMUM_CONFIDENCE_YOLO;
                        break;
                }
                List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();
                for (Classifier.Recognition result : results) {
                    RectF location = result.getLocation();
                    if (location != null && result.getConfidence().floatValue() >= minimumConfidence) {
                        canvas.drawRect(location, paint);
                        DetectorActivity.this.cropToFrameTransform.mapRect(location);
                        result.setLocation(location);
                        mappedRecognitions.add(result);
                    }
                }
                DetectorActivity.this.tracker.trackResults(mappedRecognitions, DetectorActivity.this.luminanceCopy, j);
                DetectorActivity.this.trackingOverlay.postInvalidate();
                DetectorActivity.this.requestRender();
                boolean unused3 = DetectorActivity.this.computingDetection = false;
            }
        });
    }

    /* renamed from: org.tensorflow.demo.DetectorActivity$4 */
    static /* synthetic */ class C02814 {
        static final /* synthetic */ int[] $SwitchMap$org$tensorflow$demo$DetectorActivity$DetectorMode = new int[DetectorMode.values().length];

        static {
            try {
                $SwitchMap$org$tensorflow$demo$DetectorActivity$DetectorMode[DetectorMode.TF_OD_API.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$tensorflow$demo$DetectorActivity$DetectorMode[DetectorMode.MULTIBOX.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$tensorflow$demo$DetectorActivity$DetectorMode[DetectorMode.YOLO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    /* access modifiers changed from: protected */
    public Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    public void onSetDebug(boolean debug) {
        this.detector.enableStatLogging(debug);
    }
}
