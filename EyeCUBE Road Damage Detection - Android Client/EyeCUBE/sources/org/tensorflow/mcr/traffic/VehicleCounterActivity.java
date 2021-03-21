package org.tensorflow.mcr.traffic;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.p000v4.internal.view.SupportMenu;
import android.util.Size;
import android.util.TypedValue;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.TensorFlowObjectDetectionAPIModel;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.mcr.CameraActivity;
import org.ateam.eyecube.mcr.R;

public class VehicleCounterActivity extends CameraActivity implements ImageReader.OnImageAvailableListener, SeekBar.OnSeekBarChangeListener {
    private static final float ACCURACY_THRESHOLD = 0.3f;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    /* access modifiers changed from: private */
    public static final Logger LOGGER = new Logger();
    private static final boolean MAINTAIN_ASPECT = true;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private static final float TEXT_SIZE_DIP = 10.0f;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mcr_vehicle_label.txt";
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/frozen_inference_graph.pb";
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
    public boolean detectingMode = false;
    /* access modifiers changed from: private */
    public Classifier detector;
    private Matrix frameToCropTransform;
    /* access modifiers changed from: private */
    public long lastProcessingTimeMs;
    /* access modifiers changed from: private */
    public byte[] luminanceCopy;
    /* access modifiers changed from: private */
    public String modelPath;
    /* access modifiers changed from: private */
    public String phoneCode;
    private Bitmap rgbFrameBitmap = null;
    /* access modifiers changed from: private */
    public Integer sensorOrientation;
    /* access modifiers changed from: private */
    public float threshold;
    private long timestamp = 0;
    /* access modifiers changed from: private */
    public VehicleTracker tracker;
    OverlayView trackingOverlay;

    public void onPreviewSizeChosen(Size size, int rotation) {
        this.borderedText = new BorderedText(TypedValue.applyDimension(1, TEXT_SIZE_DIP, getResources().getDisplayMetrics()));
        this.borderedText.setTypeface(Typeface.MONOSPACE);
        this.tracker = new VehicleTracker(this);
        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            String prefPath = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.preference_model_id), "");
            if (new File(prefPath).exists()) {
                this.modelPath = prefPath;
            } else {
                this.modelPath = TF_OD_API_MODEL_FILE;
            }
            this.detector = TensorFlowObjectDetectionAPIModel.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (IOException e) {
            LOGGER.mo6292e("Exception initializing classifier!", e);
            Toast.makeText(getApplicationContext(), "Classifier could not be initialized", 0).show();
            finish();
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
                VehicleCounterActivity.this.tracker.draw(canvas);
                if (VehicleCounterActivity.this.isDebug()) {
                    VehicleCounterActivity.this.tracker.drawDebug(canvas);
                }
            }
        });
        addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                Vector<String> lines = new Vector<>();
                if (VehicleCounterActivity.this.isDebug()) {
                    lines.add("Frame: " + VehicleCounterActivity.this.previewWidth + "x" + VehicleCounterActivity.this.previewHeight);
                    lines.add("Crop: 300x300");
                    lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                    lines.add("Rotation: " + VehicleCounterActivity.this.sensorOrientation);
                    lines.add("PhoneCode: " + VehicleCounterActivity.this.phoneCode);
                    lines.add("Threshold: " + VehicleCounterActivity.this.threshold);
                    lines.add("Model: " + VehicleCounterActivity.this.modelPath);
                }
                lines.add("Inference time: " + VehicleCounterActivity.this.lastProcessingTimeMs + "ms");
                VehicleCounterActivity.this.borderedText.drawLines(canvas, VehicleCounterActivity.TEXT_SIZE_DIP, (float) (canvas.getHeight() - 400), lines);
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
        if (this.computingDetection || !this.detectingMode) {
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
                VehicleCounterActivity.LOGGER.mo6294i("Running detection on image " + j, new Object[0]);
                long startTime = SystemClock.uptimeMillis();
                List<Classifier.Recognition> results = VehicleCounterActivity.this.detector.recognizeImage(VehicleCounterActivity.this.croppedBitmap);
                long unused = VehicleCounterActivity.this.lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                Bitmap unused2 = VehicleCounterActivity.this.cropCopyBitmap = Bitmap.createBitmap(VehicleCounterActivity.this.croppedBitmap);
                Canvas canvas = new Canvas(VehicleCounterActivity.this.cropCopyBitmap);
                Paint paint = new Paint();
                paint.setColor(SupportMenu.CATEGORY_MASK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);
                List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();
                for (Classifier.Recognition result : results) {
                    RectF location = result.getLocation();
                    if (location != null && result.getConfidence().floatValue() >= 0.3f) {
                        canvas.drawRect(location, paint);
                        VehicleCounterActivity.this.cropToFrameTransform.mapRect(location);
                        result.setLocation(location);
                        mappedRecognitions.add(result);
                    }
                }
                VehicleCounterActivity.this.tracker.trackResults(mappedRecognitions, VehicleCounterActivity.this.luminanceCopy, j);
                VehicleCounterActivity.this.trackingOverlay.postInvalidate();
                VehicleCounterActivity.this.requestRender();
                boolean unused3 = VehicleCounterActivity.this.computingDetection = false;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToggleButton toggleRun = (ToggleButton) findViewById(R.id.toggle_run);
        toggleRun.setChecked(false);
        toggleRun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (VehicleCounterActivity.this.tracker != null) {
                    VehicleCounterActivity.this.tracker.detecting(isChecked);
                }
                boolean unused = VehicleCounterActivity.this.detectingMode = isChecked;
            }
        });
        this.detectingMode = toggleRun.isChecked();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.phoneCode = sharedPreferences.getString(getString(R.string.preference_phoneid_id), "");
        this.threshold = sharedPreferences.getFloat(getString(R.string.preference_accuracy_threthold_id), 0.3f);
        SeekBar bar = (SeekBar) findViewById(R.id.seekTheshold);
        bar.setOnSeekBarChangeListener(this);
        bar.setMax(10);
        bar.setProgress((int) (this.threshold * TEXT_SIZE_DIP));
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

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.threshold = ((float) seekBar.getProgress()) / TEXT_SIZE_DIP;
        ((TextView) findViewById(R.id.txtThreshold)).setText(String.format("Detection Threshold: %s", new Object[]{String.valueOf(this.threshold)}));
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
