package org.tensorflow.demo;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.p000v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.ateam.eyecube.mcr.R;

public class StylizeActivity extends CameraActivity implements ImageReader.OnImageAvailableListener {
    private static final boolean DEBUG_MODEL = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 720);
    private static final String INPUT_NODE = "input";
    /* access modifiers changed from: private */
    public static final Logger LOGGER = new Logger();
    private static final String MODEL_FILE = "file:///android_asset/stylize_quantized.pb";
    private static final boolean NORMALIZE_SLIDERS = true;
    private static final int NUM_STYLES = 26;
    private static final String OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid";
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    /* access modifiers changed from: private */
    public static final int[] SIZES = {128, 192, 256, 384, 512, 720};
    private static final String STYLE_NODE = "style_num";
    private static final float TEXT_SIZE_DIP = 12.0f;
    /* access modifiers changed from: private */
    public ImageGridAdapter adapter;
    /* access modifiers changed from: private */
    public boolean allZero = false;
    private BorderedText borderedText;
    /* access modifiers changed from: private */
    public Bitmap cropCopyBitmap = null;
    private Matrix cropToFrameTransform;
    /* access modifiers changed from: private */
    public Bitmap croppedBitmap = null;
    /* access modifiers changed from: private */
    public int desiredSize = 256;
    /* access modifiers changed from: private */
    public int desiredSizeIndex = -1;
    private float[] floatValues;
    /* access modifiers changed from: private */
    public int frameNum = 0;
    private Matrix frameToCropTransform;
    private GridView grid;
    private final View.OnTouchListener gridTouchAdapter = new View.OnTouchListener() {
        ImageSlider slider = null;

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case 0:
                    for (int i = 0; i < 26; i++) {
                        ImageSlider child = StylizeActivity.this.adapter.items[i];
                        Rect rect = new Rect();
                        child.getHitRect(rect);
                        if (rect.contains((int) event.getX(), (int) event.getY())) {
                            this.slider = child;
                            this.slider.setHilighted(StylizeActivity.NORMALIZE_SLIDERS);
                        }
                    }
                    break;
                case 1:
                    if (this.slider != null) {
                        this.slider.setHilighted(false);
                        this.slider = null;
                        break;
                    }
                    break;
                case 2:
                    if (this.slider != null) {
                        this.slider.getHitRect(new Rect());
                        StylizeActivity.this.setStyle(this.slider, (float) Math.min(1.0d, Math.max(0.0d, 1.0d - ((double) ((event.getY() - ((float) this.slider.getTop())) / ((float) this.slider.getHeight()))))));
                        break;
                    }
                    break;
            }
            return StylizeActivity.NORMALIZE_SLIDERS;
        }
    };
    private TensorFlowInferenceInterface inferenceInterface;
    private int initializedSize = 0;
    private int[] intValues;
    private int lastOtherStyle = 1;
    /* access modifiers changed from: private */
    public long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Integer sensorOrientation;
    private final float[] styleVals = new float[26];
    /* access modifiers changed from: private */
    public Bitmap textureCopyBitmap;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* access modifiers changed from: protected */
    public int getLayoutId() {
        return R.layout.camera_connection_fragment_stylize;
    }

    /* access modifiers changed from: protected */
    public Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(filePath));
        } catch (IOException e) {
            LOGGER.mo6292e("Error opening bitmap!", e);
            return null;
        }
    }

    private class ImageSlider extends ImageView {
        private final Paint boxPaint;
        private boolean hilighted;
        private final Paint linePaint;
        /* access modifiers changed from: private */
        public float value;

        public ImageSlider(Context context) {
            super(context);
            this.value = 0.0f;
            this.hilighted = false;
            this.value = 0.0f;
            this.boxPaint = new Paint();
            this.boxPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
            this.boxPaint.setAlpha(128);
            this.linePaint = new Paint();
            this.linePaint.setColor(-1);
            this.linePaint.setStrokeWidth(10.0f);
            this.linePaint.setStyle(Paint.Style.STROKE);
        }

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float y = (1.0f - this.value) * ((float) canvas.getHeight());
            if (!StylizeActivity.this.allZero) {
                canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), y, this.boxPaint);
            }
            if (this.value > 0.0f) {
                canvas.drawLine(0.0f, y, (float) canvas.getWidth(), y, this.linePaint);
            }
            if (this.hilighted) {
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.linePaint);
            }
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
        }

        public void setValue(float value2) {
            this.value = value2;
            postInvalidate();
        }

        public void setHilighted(boolean highlighted) {
            this.hilighted = highlighted;
            postInvalidate();
        }
    }

    private class ImageGridAdapter extends BaseAdapter {
        final ArrayList<Button> buttons;
        final ImageSlider[] items;

        private ImageGridAdapter() {
            this.items = new ImageSlider[26];
            this.buttons = new ArrayList<>();
            final Button sizeButton = new Button(StylizeActivity.this) {
                /* access modifiers changed from: protected */
                public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
                }
            };
            sizeButton.setText("" + StylizeActivity.this.desiredSize);
            sizeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int unused = StylizeActivity.this.desiredSizeIndex = (StylizeActivity.this.desiredSizeIndex + 1) % StylizeActivity.SIZES.length;
                    int unused2 = StylizeActivity.this.desiredSize = StylizeActivity.SIZES[StylizeActivity.this.desiredSizeIndex];
                    sizeButton.setText("" + StylizeActivity.this.desiredSize);
                    sizeButton.postInvalidate();
                }
            });
            Button saveButton = new Button(StylizeActivity.this) {
                /* access modifiers changed from: protected */
                public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
                }
            };
            saveButton.setText("save");
            saveButton.setTextSize(StylizeActivity.TEXT_SIZE_DIP);
            saveButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (StylizeActivity.this.textureCopyBitmap != null) {
                        ImageUtils.saveBitmap(StylizeActivity.this.textureCopyBitmap, "stylized" + StylizeActivity.this.frameNum + ".png");
                        Toast.makeText(StylizeActivity.this, "Saved image to: /sdcard/tensorflow/stylized" + StylizeActivity.this.frameNum + ".png", 1).show();
                    }
                }
            });
            this.buttons.add(sizeButton);
            this.buttons.add(saveButton);
            for (int i = 0; i < 26; i++) {
                StylizeActivity.LOGGER.mo6298v("Creating item %d", Integer.valueOf(i));
                if (this.items[i] == null) {
                    ImageSlider slider = new ImageSlider(StylizeActivity.this);
                    slider.setImageBitmap(StylizeActivity.getBitmapFromAsset(StylizeActivity.this, "thumbnails/style" + i + ".jpg"));
                    this.items[i] = slider;
                }
            }
        }

        public int getCount() {
            return this.buttons.size() + 26;
        }

        public Object getItem(int position) {
            if (position < this.buttons.size()) {
                return this.buttons.get(position);
            }
            return this.items[position - this.buttons.size()];
        }

        public long getItemId(int position) {
            return (long) getItem(position).hashCode();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return convertView != null ? convertView : (View) getItem(position);
        }
    }

    public void onPreviewSizeChosen(Size size, int rotation) {
        this.borderedText = new BorderedText(TypedValue.applyDimension(1, TEXT_SIZE_DIP, getResources().getDisplayMetrics()));
        this.borderedText.setTypeface(Typeface.MONOSPACE);
        this.inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);
        this.previewWidth = size.getWidth();
        this.previewHeight = size.getHeight();
        int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();
        LOGGER.mo6294i("Sensor orientation: %d, Screen orientation: %d", Integer.valueOf(rotation), Integer.valueOf(screenOrientation));
        this.sensorOrientation = Integer.valueOf(rotation + screenOrientation);
        addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                StylizeActivity.this.renderDebug(canvas);
            }
        });
        this.adapter = new ImageGridAdapter();
        this.grid = (GridView) findViewById(R.id.grid_layout);
        this.grid.setAdapter(this.adapter);
        this.grid.setOnTouchListener(this.gridTouchAdapter);
        if (((UiModeManager) getSystemService("uimode")).getCurrentModeType() == 4) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int styleSelectorHeight = displayMetrics.heightPixels;
            int styleSelectorWidth = displayMetrics.widthPixels - styleSelectorHeight;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(styleSelectorWidth, -1);
            int numOfStylePerRow = 3;
            while (((double) (styleSelectorWidth / numOfStylePerRow)) * Math.ceil((double) (((float) (this.adapter.getCount() - 2)) / ((float) numOfStylePerRow))) > ((double) styleSelectorHeight)) {
                numOfStylePerRow++;
            }
            this.grid.setNumColumns(numOfStylePerRow);
            layoutParams.addRule(11);
            this.grid.setLayoutParams(layoutParams);
            this.adapter.buttons.clear();
        }
        setStyle(this.adapter.items[0], 1.0f);
    }

    /* access modifiers changed from: private */
    public void setStyle(ImageSlider slider, float value) {
        slider.setValue(value);
        float otherSum = 0.0f;
        for (int i = 0; i < 26; i++) {
            if (this.adapter.items[i] != slider) {
                otherSum += this.adapter.items[i].value;
            }
        }
        if (((double) otherSum) > 0.0d) {
            float highestOtherVal = 0.0f;
            float factor = otherSum > 0.0f ? (1.0f - value) / otherSum : 0.0f;
            for (int i2 = 0; i2 < 26; i2++) {
                ImageSlider child = this.adapter.items[i2];
                if (child != slider) {
                    float newVal = child.value * factor;
                    if (newVal <= 0.01f) {
                        newVal = 0.0f;
                    }
                    child.setValue(newVal);
                    if (child.value > highestOtherVal) {
                        this.lastOtherStyle = i2;
                        highestOtherVal = child.value;
                    }
                }
            }
        } else {
            if (this.adapter.items[this.lastOtherStyle] == slider) {
                this.lastOtherStyle = (this.lastOtherStyle + 1) % 26;
            }
            this.adapter.items[this.lastOtherStyle].setValue(1.0f - value);
        }
        boolean lastAllZero = this.allZero;
        float sum = 0.0f;
        for (int i3 = 0; i3 < 26; i3++) {
            sum += this.adapter.items[i3].value;
        }
        this.allZero = sum == 0.0f ? NORMALIZE_SLIDERS : false;
        for (int i4 = 0; i4 < 26; i4++) {
            this.styleVals[i4] = this.allZero ? 0.03846154f : this.adapter.items[i4].value / sum;
            if (lastAllZero != this.allZero) {
                this.adapter.items[i4].postInvalidate();
            }
        }
    }

    private void resetPreviewBuffers() {
        this.croppedBitmap = Bitmap.createBitmap(this.desiredSize, this.desiredSize, Bitmap.Config.ARGB_8888);
        this.frameToCropTransform = ImageUtils.getTransformationMatrix(this.previewWidth, this.previewHeight, this.desiredSize, this.desiredSize, this.sensorOrientation.intValue(), NORMALIZE_SLIDERS);
        this.cropToFrameTransform = new Matrix();
        this.frameToCropTransform.invert(this.cropToFrameTransform);
        this.intValues = new int[(this.desiredSize * this.desiredSize)];
        this.floatValues = new float[(this.desiredSize * this.desiredSize * 3)];
        this.initializedSize = this.desiredSize;
    }

    /* access modifiers changed from: protected */
    public void processImage() {
        if (this.desiredSize != this.initializedSize) {
            LOGGER.mo6294i("Initializing at size preview size %dx%d, stylize size %d", Integer.valueOf(this.previewWidth), Integer.valueOf(this.previewHeight), Integer.valueOf(this.desiredSize));
            this.rgbFrameBitmap = Bitmap.createBitmap(this.previewWidth, this.previewHeight, Bitmap.Config.ARGB_8888);
            this.croppedBitmap = Bitmap.createBitmap(this.desiredSize, this.desiredSize, Bitmap.Config.ARGB_8888);
            this.frameToCropTransform = ImageUtils.getTransformationMatrix(this.previewWidth, this.previewHeight, this.desiredSize, this.desiredSize, this.sensorOrientation.intValue(), NORMALIZE_SLIDERS);
            this.cropToFrameTransform = new Matrix();
            this.frameToCropTransform.invert(this.cropToFrameTransform);
            this.intValues = new int[(this.desiredSize * this.desiredSize)];
            this.floatValues = new float[(this.desiredSize * this.desiredSize * 3)];
            this.initializedSize = this.desiredSize;
        }
        this.rgbFrameBitmap.setPixels(getRgbBytes(), 0, this.previewWidth, 0, 0, this.previewWidth, this.previewHeight);
        new Canvas(this.croppedBitmap).drawBitmap(this.rgbFrameBitmap, this.frameToCropTransform, (Paint) null);
        runInBackground(new Runnable() {
            public void run() {
                Bitmap unused = StylizeActivity.this.cropCopyBitmap = Bitmap.createBitmap(StylizeActivity.this.croppedBitmap);
                long startTime = SystemClock.uptimeMillis();
                StylizeActivity.this.stylizeImage(StylizeActivity.this.croppedBitmap);
                long unused2 = StylizeActivity.this.lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                Bitmap unused3 = StylizeActivity.this.textureCopyBitmap = Bitmap.createBitmap(StylizeActivity.this.croppedBitmap);
                StylizeActivity.this.requestRender();
                StylizeActivity.this.readyForNextImage();
            }
        });
        if (this.desiredSize != this.initializedSize) {
            resetPreviewBuffers();
        }
    }

    /* access modifiers changed from: private */
    public void stylizeImage(Bitmap bitmap) {
        this.frameNum++;
        bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < this.intValues.length; i++) {
            int val = this.intValues[i];
            this.floatValues[i * 3] = ((float) ((val >> 16) & 255)) / 255.0f;
            this.floatValues[(i * 3) + 1] = ((float) ((val >> 8) & 255)) / 255.0f;
            this.floatValues[(i * 3) + 2] = ((float) (val & 255)) / 255.0f;
        }
        LOGGER.mo6294i("Width: %s , Height: %s", Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight()));
        this.inferenceInterface.feed(INPUT_NODE, this.floatValues, 1, (long) bitmap.getWidth(), (long) bitmap.getHeight(), 3);
        this.inferenceInterface.feed(STYLE_NODE, this.styleVals, 26);
        this.inferenceInterface.run(new String[]{OUTPUT_NODE}, isDebug());
        this.inferenceInterface.fetch(OUTPUT_NODE, this.floatValues);
        for (int i2 = 0; i2 < this.intValues.length; i2++) {
            this.intValues[i2] = -16777216 | (((int) (this.floatValues[i2 * 3] * 255.0f)) << 16) | (((int) (this.floatValues[(i2 * 3) + 1] * 255.0f)) << 8) | ((int) (this.floatValues[(i2 * 3) + 2] * 255.0f));
        }
        bitmap.setPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    /* access modifiers changed from: private */
    public void renderDebug(Canvas canvas) {
        Bitmap copy;
        Bitmap texture = this.textureCopyBitmap;
        if (texture != null) {
            Matrix matrix = new Matrix();
            float scaleFactor = Math.min(((float) canvas.getWidth()) / ((float) texture.getWidth()), ((float) canvas.getHeight()) / ((float) texture.getHeight()));
            matrix.postScale(scaleFactor, scaleFactor);
            canvas.drawBitmap(texture, matrix, new Paint());
        }
        if (isDebug() && (copy = this.cropCopyBitmap) != null) {
            canvas.drawColor(1426063360);
            Matrix matrix2 = new Matrix();
            matrix2.postScale(2.0f, 2.0f);
            matrix2.postTranslate(((float) canvas.getWidth()) - (((float) copy.getWidth()) * 2.0f), ((float) canvas.getHeight()) - (((float) copy.getHeight()) * 2.0f));
            canvas.drawBitmap(copy, matrix2, new Paint());
            Vector<String> lines = new Vector<>();
            Collections.addAll(lines, this.inferenceInterface.getStatString().split("\n"));
            lines.add("");
            lines.add("Frame: " + this.previewWidth + "x" + this.previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + this.sensorOrientation);
            lines.add("Inference time: " + this.lastProcessingTimeMs + "ms");
            lines.add("Desired size: " + this.desiredSize);
            lines.add("Initialized size: " + this.initializedSize);
            this.borderedText.drawLines(canvas, 10.0f, (float) (canvas.getHeight() - 10), lines);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int moveOffset;
        switch (keyCode) {
            case 19:
                moveOffset = this.grid.getNumColumns() * -1;
                break;
            case 20:
                moveOffset = this.grid.getNumColumns();
                break;
            case 21:
                moveOffset = -1;
                break;
            case 22:
                moveOffset = 1;
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        int currentSelect = 0;
        float highestValue = 0.0f;
        for (int i = 0; i < this.adapter.getCount(); i++) {
            if (this.adapter.items[i].value > highestValue) {
                currentSelect = i;
                highestValue = this.adapter.items[i].value;
            }
        }
        setStyle(this.adapter.items[((currentSelect + moveOffset) + this.adapter.getCount()) % this.adapter.getCount()], 1.0f);
        return NORMALIZE_SLIDERS;
    }
}
