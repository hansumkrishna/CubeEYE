package org.tensorflow.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ImageReader;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import java.util.List;
import java.util.Vector;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.ateam.eyecube.mcr.R;

public class ClassifierActivity extends CameraActivity implements ImageReader.OnImageAvailableListener {
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1.0f;
    private static final String INPUT_NAME = "input";
    private static final int INPUT_SIZE = 224;
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    /* access modifiers changed from: private */
    public static final Logger LOGGER = new Logger();
    private static final boolean MAINTAIN_ASPECT = true;
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String OUTPUT_NAME = "output";
    protected static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10.0f;
    private BorderedText borderedText;
    /* access modifiers changed from: private */
    public Classifier classifier;
    /* access modifiers changed from: private */
    public Bitmap cropCopyBitmap = null;
    private Matrix cropToFrameTransform;
    /* access modifiers changed from: private */
    public Bitmap croppedBitmap = null;
    private Matrix frameToCropTransform;
    /* access modifiers changed from: private */
    public long lastProcessingTimeMs;
    /* access modifiers changed from: private */
    public ResultsView resultsView;
    private Bitmap rgbFrameBitmap = null;
    private Integer sensorOrientation;

    /* access modifiers changed from: protected */
    public int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    /* access modifiers changed from: protected */
    public Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    public void onPreviewSizeChosen(Size size, int rotation) {
        this.borderedText = new BorderedText(TypedValue.applyDimension(1, TEXT_SIZE_DIP, getResources().getDisplayMetrics()));
        this.borderedText.setTypeface(Typeface.MONOSPACE);
        this.classifier = TensorFlowImageClassifier.create(getAssets(), MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
        this.previewWidth = size.getWidth();
        this.previewHeight = size.getHeight();
        this.sensorOrientation = Integer.valueOf(rotation - getScreenOrientation());
        LOGGER.mo6294i("Camera orientation relative to screen canvas: %d", this.sensorOrientation);
        LOGGER.mo6294i("Initializing at size %dx%d", Integer.valueOf(this.previewWidth), Integer.valueOf(this.previewHeight));
        this.rgbFrameBitmap = Bitmap.createBitmap(this.previewWidth, this.previewHeight, Bitmap.Config.ARGB_8888);
        this.croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);
        this.frameToCropTransform = ImageUtils.getTransformationMatrix(this.previewWidth, this.previewHeight, INPUT_SIZE, INPUT_SIZE, this.sensorOrientation.intValue(), MAINTAIN_ASPECT);
        this.cropToFrameTransform = new Matrix();
        this.frameToCropTransform.invert(this.cropToFrameTransform);
        addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                ClassifierActivity.this.renderDebug(canvas);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void processImage() {
        this.rgbFrameBitmap.setPixels(getRgbBytes(), 0, this.previewWidth, 0, 0, this.previewWidth, this.previewHeight);
        new Canvas(this.croppedBitmap).drawBitmap(this.rgbFrameBitmap, this.frameToCropTransform, (Paint) null);
        runInBackground(new Runnable() {
            public void run() {
                long startTime = SystemClock.uptimeMillis();
                List<Classifier.Recognition> results = ClassifierActivity.this.classifier.recognizeImage(ClassifierActivity.this.croppedBitmap);
                long unused = ClassifierActivity.this.lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                ClassifierActivity.LOGGER.mo6294i("Detect: %s", results);
                Bitmap unused2 = ClassifierActivity.this.cropCopyBitmap = Bitmap.createBitmap(ClassifierActivity.this.croppedBitmap);
                if (ClassifierActivity.this.resultsView == null) {
                    ResultsView unused3 = ClassifierActivity.this.resultsView = (ResultsView) ClassifierActivity.this.findViewById(R.id.results);
                }
                ClassifierActivity.this.resultsView.setResults(results);
                ClassifierActivity.this.requestRender();
                ClassifierActivity.this.readyForNextImage();
            }
        });
    }

    public void onSetDebug(boolean debug) {
        this.classifier.enableStatLogging(debug);
    }

    /* access modifiers changed from: private */
    public void renderDebug(Canvas canvas) {
        Bitmap copy;
        if (isDebug() && (copy = this.cropCopyBitmap) != null) {
            Matrix matrix = new Matrix();
            matrix.postScale(2.0f, 2.0f);
            matrix.postTranslate(((float) canvas.getWidth()) - (((float) copy.getWidth()) * 2.0f), ((float) canvas.getHeight()) - (((float) copy.getHeight()) * 2.0f));
            canvas.drawBitmap(copy, matrix, new Paint());
            Vector<String> lines = new Vector<>();
            if (this.classifier != null) {
                for (String line : this.classifier.getStatString().split("\n")) {
                    lines.add(line);
                }
            }
            lines.add("Frame: " + this.previewWidth + "x" + this.previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + this.sensorOrientation);
            lines.add("Inference time: " + this.lastProcessingTimeMs + "ms");
            this.borderedText.drawLines(canvas, TEXT_SIZE_DIP, (float) (canvas.getHeight() - 10), lines);
        }
    }
}
