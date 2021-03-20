package org.tensorflow.mcr.road;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.TensorFlowObjectDetectionAPIModel;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.mcr.CameraActivity;
import org.tensorflow.mcr.road.server.RTUploadDataTask;
import org.ateam.eyecube.mcr.R;

public class RoadDamageDetectorActivity extends CameraActivity implements ImageReader.OnImageAvailableListener, LocationListener {
    private static final float ACCURACY_THRESHOLD = 0.5f;
    private static final int ACCURACY_THRESHOLD_OFFSET = 2;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1620, 1080);
    private static final int LOCATIONS_NUM = 10;
    private static final int LOCATION_UPDATE_DISTANCE = 0;
    private static final int LOCATION_UPDATE_TIME = 2000;
    private static final boolean MAINTAIN_ASPECT = true;
    private static final int PROCESS_MODE_MOVIE = 2131361897;
    private static final int PROCESS_MODE_SERVER = 2131361895;
    private static final int PROCESS_MODE_STORAGE = 2131361896;
    private static final double SPEED_TOLERANCE = 2.5d;
    private static final float TEXT_SIZE_DIP = 10.0f;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/mcr_crack_label.txt";
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_RoadDamageDetector.pb";
    private static boolean closedMode;
    private static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
    private static RTUploadDataTask uploader;
    /* access modifiers changed from: private */
    public BorderedText borderedText;
    /* access modifiers changed from: private */
    public boolean computingDetection = false;
    /* access modifiers changed from: private */
    public ConvImage croppedImage;
    /* access modifiers changed from: private */
    public Date currentDate = null;
    /* access modifiers changed from: private */
    public boolean detecting = false;
    /* access modifiers changed from: private */
    public Classifier detector;
    /* access modifiers changed from: private */
    public String deviceId;
    /* access modifiers changed from: private */
    public Location lastLocation;
    /* access modifiers changed from: private */
    public long lastProcessingTimeMs;
    private LocationManager locationManager;
    private List<Location> locations;
    /* access modifiers changed from: private */
    public byte[] luminanceCopy;
    private String modelpath = TF_OD_API_MODEL_FILE;
    /* access modifiers changed from: private */
    public boolean moving = false;
    /* access modifiers changed from: private */
    public boolean movingMode;
    /* access modifiers changed from: private */
    public String phoneCode;
    /* access modifiers changed from: private */
    public int processMode;
    private Bitmap rgbFrameBitmap = null;
    private Integer sensorOrientation;
    private Date startDate = null;
    /* access modifiers changed from: private */
    public ConvImage targetImage;
    /* access modifiers changed from: private */
    public float threshold;
    private long timestamp = 0;
    /* access modifiers changed from: private */
    public MultiBoxTracker tracker = null;
    OverlayView trackingOverlay;

    public void onPreviewSizeChosen(Size size, int rotation) {
        this.borderedText = new BorderedText(TypedValue.applyDimension(1, TEXT_SIZE_DIP, getResources().getDisplayMetrics()));
        this.borderedText.setTypeface(Typeface.MONOSPACE);
        this.tracker = new MultiBoxTracker(this);
        try {
            String prefPath = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.preference_model_id), TF_OD_API_MODEL_FILE);
            if (new File(prefPath).exists()) {
                this.modelpath = prefPath;
            }
            this.detector = TensorFlowObjectDetectionAPIModel.create(getAssets(), this.modelpath, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            ThrowableExtension.printStackTrace(e);
            finish();
        }
        this.previewWidth = size.getWidth();
        this.previewHeight = size.getHeight();
        this.sensorOrientation = Integer.valueOf(rotation - getScreenOrientation());
        this.rgbFrameBitmap = Bitmap.createBitmap(this.previewWidth, this.previewHeight, Bitmap.Config.ARGB_8888);
        this.croppedImage = new ConvImage(ImageMatrixUtils.getTransformationMatrix(this.previewWidth, this.previewHeight, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, this.sensorOrientation.intValue(), MAINTAIN_ASPECT), TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE);
        int minSize = Math.min(this.previewWidth, this.previewHeight);
        Matrix frameToTargetTransform = ImageMatrixUtils.getTransformationMatrix(this.previewWidth, this.previewHeight, minSize, minSize, this.sensorOrientation.intValue(), MAINTAIN_ASPECT);
        frameToTargetTransform.postTranslate(0.0f, (float) ((-minSize) / 2));
        this.targetImage = new ConvImage(frameToTargetTransform, minSize, minSize / 2);
        List<RectF> cropAreas = new ArrayList<>();
        RectF cropArea = new RectF(0.0f, 0.0f, (float) this.croppedImage.getWidth(), (float) this.croppedImage.getHeight());
        this.croppedImage.toFrameTransform.mapRect(cropArea);
        cropAreas.add(cropArea);
        RectF rectF = new RectF(0.0f, 0.0f, (float) this.targetImage.getWidth(), (float) this.targetImage.getHeight());
        this.targetImage.toFrameTransform.mapRect(rectF);
        cropAreas.add(rectF);
        this.tracker.setRectMode(this.processMode != R.id.optMode3 ? MAINTAIN_ASPECT : false);
        this.tracker.setCropAreas(cropAreas);
        this.trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        this.trackingOverlay.addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                RoadDamageDetectorActivity.this.tracker.draw(canvas);
            }
        });
        addCallback(new OverlayView.DrawCallback() {
            public void drawCallback(Canvas canvas) {
                Vector<String> lines = new Vector<>();
                lines.add("Frame: " + RoadDamageDetectorActivity.this.previewWidth + "x" + RoadDamageDetectorActivity.this.previewHeight);
                lines.add("Crop: 300");
                lines.add("Inference time: " + RoadDamageDetectorActivity.this.lastProcessingTimeMs + "ms");
                lines.add("PhoneCode: " + RoadDamageDetectorActivity.this.phoneCode);
                lines.add("IMEI: " + RoadDamageDetectorActivity.this.deviceId);
                lines.add("Moving: " + RoadDamageDetectorActivity.this.moving);
                lines.add("GPS location: " + (RoadDamageDetectorActivity.this.lastLocation != null ? RoadDamageDetectorActivity.MAINTAIN_ASPECT : false));
                RoadDamageDetectorActivity.this.borderedText.drawLines(canvas, 20.0f, 200.0f, lines);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void processImage() {
        this.currentDate = new Date();
        this.timestamp++;
        long currTimestamp = this.timestamp;
        byte[] originalLuminance = getLuminance();
        this.tracker.onFrame(this.previewWidth, this.previewHeight, getLuminanceStride(), this.sensorOrientation.intValue(), originalLuminance, this.timestamp);
        this.trackingOverlay.postInvalidate();
        if (this.computingDetection || !this.detecting || this.lastLocation == null || (!this.moving && this.movingMode)) {
            readyForNextImage();
            return;
        }
        this.computingDetection = MAINTAIN_ASPECT;
        this.rgbFrameBitmap.setPixels(getRgbBytes(), 0, this.previewWidth, 0, 0, this.previewWidth, this.previewHeight);
        if (this.luminanceCopy == null) {
            this.luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, this.luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();
        Location location = new Location(this.lastLocation);
        if (this.processMode != R.id.optMode3) {
            new Canvas(this.croppedImage.bitmap).drawBitmap(this.rgbFrameBitmap, this.croppedImage.frameToTransform, (Paint) null);
        } else {
            new Canvas(this.targetImage.bitmap).drawBitmap(this.rgbFrameBitmap, this.targetImage.frameToTransform, (Paint) null);
        }
        final Location location2 = location;
        final long j = currTimestamp;
        runInBackground(new Runnable() {
            public void run() {
                long startTime = SystemClock.uptimeMillis();
                if (RoadDamageDetectorActivity.this.processMode == R.id.optMode3) {
                    File dir = RoadDamageDetectorActivity.this.getSaveLocation();
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    try {
                        Thread.sleep(600);
                    } catch (Exception e) {
                        ThrowableExtension.printStackTrace(e);
                    }
                    RoadImage.save(RoadDamageDetectorActivity.this.targetImage.bitmap, new File(dir, RoadDamageDetectorActivity.this.getFileName("img") + ".jpg"));
                } else {
                    List<Classifier.Recognition> results = RoadDamageDetectorActivity.this.detector.recognizeImage(RoadDamageDetectorActivity.this.croppedImage.bitmap);
                    Iterator<Classifier.Recognition> iter = results.iterator();
                    while (iter.hasNext()) {
                        if (iter.next().getConfidence().floatValue() < RoadDamageDetectorActivity.ACCURACY_THRESHOLD) {
                            iter.remove();
                        }
                    }
                    List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();
                    if (results.size() > 0) {
                        RoadDamageDetectorActivity.this.saveDamageData(results, RoadDamageDetectorActivity.this.croppedImage, RoadDamageDetectorActivity.this.currentDate, location2);
                        for (Classifier.Recognition result : results) {
                            RectF location = result.getLocation();
                            RoadDamageDetectorActivity.this.croppedImage.toFrameTransform.mapRect(location);
                            result.setLocation(location);
                            mappedRecognitions.add(result);
                        }
                    }
                    RoadDamageDetectorActivity.this.tracker.trackResults(mappedRecognitions, RoadDamageDetectorActivity.this.luminanceCopy, j);
                }
                long unused = RoadDamageDetectorActivity.this.lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                RoadDamageDetectorActivity.this.trackingOverlay.postInvalidate();
                RoadDamageDetectorActivity.this.requestRender();
                boolean unused2 = RoadDamageDetectorActivity.this.computingDetection = false;
            }
        });
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

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        getLayoutInflater().inflate(R.layout.mcr_activity_camera_menu, linearLayout);
        addContentView(linearLayout, new FrameLayout.LayoutParams(-1, -1));
        ToggleButton toggleRun = (ToggleButton) findViewById(R.id.toggle_run);
        toggleRun.setChecked(false);
        toggleRun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RoadDamageDetectorActivity.this.toggleRun_OnCheckedChangeListener(isChecked);
            }
        });
        this.detecting = toggleRun.isChecked();
        closedMode = getResources().getBoolean(R.bool.closed_mode);
        ((LinearLayout) findViewById(R.id.layoutMenu13)).setVisibility(closedMode ? 0 : 4);
        ((RadioButton) findViewById(R.id.optMode1)).setVisibility(closedMode ? 0 : 4);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.phoneCode = sharedPreferences.getString(getString(R.string.preference_phoneid_id), "anonymous");
        this.threshold = sharedPreferences.getFloat(getString(R.string.preference_accuracy_threthold_id), ACCURACY_THRESHOLD);
        this.processMode = sharedPreferences.getInt(getString(R.string.preference_process_mode_id), closedMode ? R.id.optMode1 : R.id.optMode2);
        this.movingMode = sharedPreferences.getBoolean(getString(R.string.preference_moving_mode_id), closedMode ? MAINTAIN_ASPECT : false);
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        if (checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            finish();
        } else {
            this.deviceId = tm.getDeviceId();
        }
        SeekBar seekThreshold = (SeekBar) findViewById(R.id.seekTheshold);
        seekThreshold.setMax(5);
        final TextView txtThreshold = (TextView) findViewById(R.id.txtThreshold);
        seekThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float unused = RoadDamageDetectorActivity.this.threshold = ((float) (seekBar.getProgress() + 2)) / RoadDamageDetectorActivity.TEXT_SIZE_DIP;
                txtThreshold.setText(RoadDamageDetectorActivity.this.getThresholdString());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekThreshold.setProgress(((int) (this.threshold * TEXT_SIZE_DIP)) - 2);
        final RadioGroup radioMode = (RadioGroup) findViewById(R.id.radioMode);
        radioMode.check(this.processMode);
        radioMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int unused = RoadDamageDetectorActivity.this.processMode = radioMode.getCheckedRadioButtonId();
                if (RoadDamageDetectorActivity.this.tracker != null) {
                    RoadDamageDetectorActivity.this.tracker.setRectMode(RoadDamageDetectorActivity.this.processMode != R.id.optMode3 ? RoadDamageDetectorActivity.MAINTAIN_ASPECT : false);
                }
            }
        });
        CheckBox checkMoving = (CheckBox) findViewById(R.id.checkMoving);
        checkMoving.setChecked(this.movingMode);
        checkMoving.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean unused = RoadDamageDetectorActivity.this.movingMode = isChecked;
            }
        });
        final TextView txtPhoneCode = (TextView) findViewById(R.id.txtUserId);
        txtPhoneCode.setText(createDisplayPhonecode(this.phoneCode));
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        txtPhoneCode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!RoadDamageDetectorActivity.this.detecting) {
                    final EditText editView = new EditText(RoadDamageDetectorActivity.this);
                    dialog.setTitle("Please enter your username");
                    dialog.setView(editView);
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String unused = RoadDamageDetectorActivity.this.phoneCode = editView.getText().toString();
                            if (RoadDamageDetectorActivity.this.phoneCode.equals("")) {
                                String unused2 = RoadDamageDetectorActivity.this.phoneCode = "anonymous";
                            }
                            txtPhoneCode.setText(RoadDamageDetectorActivity.this.createDisplayPhonecode(RoadDamageDetectorActivity.this.phoneCode));
                        }
                    });
                    dialog.show();
                }
            }
        });
        ((TextView) findViewById(R.id.txtUpload)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!RoadDamageDetectorActivity.this.detecting) {
                    RoadDamageDetectorActivity.this.openUploadActivity();
                }
            }
        });
        this.currentDate = new Date();
        uploader = null;
        System.setProperty("http.maxConnections", "10");
    }

    /* access modifiers changed from: private */
    public String getThresholdString() {
        return String.format("CF:%s", new Object[]{String.valueOf(this.threshold)});
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* access modifiers changed from: private */
    public String createDisplayPhonecode(String userName) {
        return String.format("ID:%s", new Object[]{userName});
    }

    private void updatePreference() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getString(R.string.preference_phoneid_id), this.phoneCode);
        editor.putFloat(getString(R.string.preference_accuracy_threthold_id), this.threshold);
        editor.putInt(getString(R.string.preference_process_mode_id), this.processMode);
        editor.putBoolean(getString(R.string.preference_moving_mode_id), this.movingMode);
        editor.apply();
    }

    private void prepareView(boolean mode) {
        boolean enable = !mode ? MAINTAIN_ASPECT : false;
        ((TextView) findViewById(R.id.txtUserId)).setEnabled(enable);
        ((TextView) findViewById(R.id.txtUpload)).setEnabled(enable);
        ((RadioButton) findViewById(R.id.optMode1)).setEnabled(enable);
        ((RadioButton) findViewById(R.id.optMode2)).setEnabled(enable);
        ((RadioButton) findViewById(R.id.optMode3)).setEnabled(enable);
        ((CheckBox) findViewById(R.id.checkMoving)).setEnabled(enable);
        ((SeekBar) findViewById(R.id.seekTheshold)).setEnabled(enable);
    }

    private void prepareForDetection(boolean mode) {
        prepareView(mode);
        if (mode) {
            this.lastLocation = new Location("test");
            this.lastLocation.setLatitude(0.0d);
            this.lastLocation.setLongitude(0.0d);
            this.startDate = new Date();
            this.locations = new ArrayList();
            updatePreference();
            startGpsLogging();
            this.tracker.setProcessing(MAINTAIN_ASPECT);
            if (this.processMode == R.id.optMode1) {
                uploader = new RTUploadDataTask();
                uploader.ready(this, this.phoneCode, getSaveLocation());
                uploader.execute(new String[0]);
                return;
            }
            if (this.processMode == R.id.optMode2 || this.processMode == R.id.optMode3) {
            }
            return;
        }
        if (uploader != null) {
            uploader.cancel(MAINTAIN_ASPECT);
        }
        this.tracker.setProcessing(false);
        if (this.processMode == R.id.optMode3) {
        }
        stopGpsLogging();
    }

    public void toggleRun_OnCheckedChangeListener(boolean isChecked) {
        prepareForDetection(isChecked);
        this.detecting = isChecked;
    }

    /* access modifiers changed from: private */
    public void openUploadActivity() {
        Intent intent = new Intent(getApplication(), UploadActivity.class);
        intent.putExtra("phoneCode", this.phoneCode);
        startActivity(intent);
    }

    public synchronized void onPause() {
        super.onPause();
        this.detecting = false;
        stopGpsLogging();
        if (uploader != null) {
            uploader.cancel(MAINTAIN_ASPECT);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    private void startGpsLogging() {
        this.locationManager = (LocationManager) getSystemService("location");
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != 0) {
            finish();
        }
        LocationManager locationManager2 = this.locationManager;
        LocationManager locationManager3 = this.locationManager;
        locationManager2.requestLocationUpdates("gps", 2000, 0.0f, this);
    }

    private void stopGpsLogging() {
        if (this.locationManager != null) {
            this.locationManager.removeUpdates(this);
        }
    }

    public void onLocationChanged(Location location) {
        this.lastLocation = new Location(location);
        this.moving = (!location.hasSpeed() || ((double) location.getSpeed()) <= SPEED_TOLERANCE) ? false : MAINTAIN_ASPECT;
        if (this.moving || this.processMode == R.id.optMode3 || this.locations.size() == 0) {
            this.locations.add(this.lastLocation);
        }
        if (this.locations.size() >= 10) {
            saveLocations();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00f7, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00f8, code lost:
        r19 = r6;
        r6 = r5;
        r5 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0101, code lost:
        r5 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveDamageData(java.util.List<org.tensorflow.demo.Classifier.Recognition> r21, org.tensorflow.mcr.road.ConvImage r22, java.util.Date r23, android.location.Location r24) {
        /*
            r20 = this;
            org.tensorflow.mcr.road.log.DamageSize r2 = new org.tensorflow.mcr.road.log.DamageSize     // Catch:{ Exception -> 0x0051 }
            r5 = 3
            int r6 = r22.getHeight()     // Catch:{ Exception -> 0x0051 }
            int r8 = r22.getWidth()     // Catch:{ Exception -> 0x0051 }
            r2.<init>(r5, r6, r8)     // Catch:{ Exception -> 0x0051 }
            java.util.ArrayList r11 = new java.util.ArrayList     // Catch:{ Exception -> 0x0051 }
            r11.<init>()     // Catch:{ Exception -> 0x0051 }
            java.util.Iterator r12 = r21.iterator()     // Catch:{ Exception -> 0x0051 }
        L_0x0017:
            boolean r5 = r12.hasNext()     // Catch:{ Exception -> 0x0051 }
            if (r5 == 0) goto L_0x006f
            java.lang.Object r18 = r12.next()     // Catch:{ Exception -> 0x0051 }
            org.tensorflow.demo.Classifier$Recognition r18 = (org.tensorflow.demo.Classifier.Recognition) r18     // Catch:{ Exception -> 0x0051 }
            org.tensorflow.mcr.road.log.DamageObject r3 = new org.tensorflow.mcr.road.log.DamageObject     // Catch:{ Exception -> 0x0051 }
            android.graphics.RectF r5 = r18.getLocation()     // Catch:{ Exception -> 0x0051 }
            float r4 = r5.left     // Catch:{ Exception -> 0x0051 }
            android.graphics.RectF r5 = r18.getLocation()     // Catch:{ Exception -> 0x0051 }
            float r5 = r5.right     // Catch:{ Exception -> 0x0051 }
            android.graphics.RectF r6 = r18.getLocation()     // Catch:{ Exception -> 0x0051 }
            float r6 = r6.bottom     // Catch:{ Exception -> 0x0051 }
            android.graphics.RectF r8 = r18.getLocation()     // Catch:{ Exception -> 0x0051 }
            float r7 = r8.top     // Catch:{ Exception -> 0x0051 }
            java.lang.Float r8 = r18.getConfidence()     // Catch:{ Exception -> 0x0051 }
            float r8 = r8.floatValue()     // Catch:{ Exception -> 0x0051 }
            double r8 = (double) r8     // Catch:{ Exception -> 0x0051 }
            java.lang.String r10 = r18.getTitle()     // Catch:{ Exception -> 0x0051 }
            r3.<init>(r4, r5, r6, r7, r8, r10)     // Catch:{ Exception -> 0x0051 }
            r11.add(r3)     // Catch:{ Exception -> 0x0051 }
            goto L_0x0017
        L_0x0051:
            r14 = move-exception
            java.lang.String r5 = "Error"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = ""
            java.lang.StringBuilder r6 = r6.append(r8)
            java.lang.String r8 = r14.toString()
            java.lang.StringBuilder r6 = r6.append(r8)
            java.lang.String r6 = r6.toString()
            android.util.Log.e(r5, r6)
        L_0x006e:
            return
        L_0x006f:
            java.text.SimpleDateFormat r5 = dateFormat2     // Catch:{ Exception -> 0x0051 }
            r0 = r20
            java.util.Date r6 = r0.currentDate     // Catch:{ Exception -> 0x0051 }
            java.lang.String r7 = r5.format(r6)     // Catch:{ Exception -> 0x0051 }
            r5 = 2131492915(0x7f0c0033, float:1.8609295E38)
            r0 = r20
            java.lang.String r5 = r0.getString(r5)     // Catch:{ Exception -> 0x0051 }
            r0 = r20
            java.lang.String r16 = r0.getFileName(r5)     // Catch:{ Exception -> 0x0051 }
            org.tensorflow.mcr.road.log.DamageInfo r4 = new org.tensorflow.mcr.road.log.DamageInfo     // Catch:{ Exception -> 0x0051 }
            r0 = r20
            java.lang.String r5 = r0.deviceId     // Catch:{ Exception -> 0x0051 }
            r0 = r20
            java.lang.String r6 = r0.phoneCode     // Catch:{ Exception -> 0x0051 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0051 }
            r8.<init>()     // Catch:{ Exception -> 0x0051 }
            r0 = r16
            java.lang.StringBuilder r8 = r8.append(r0)     // Catch:{ Exception -> 0x0051 }
            java.lang.String r9 = ".jpg"
            java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Exception -> 0x0051 }
            java.lang.String r9 = r8.toString()     // Catch:{ Exception -> 0x0051 }
            r0 = r22
            android.graphics.Bitmap r8 = r0.bitmap     // Catch:{ Exception -> 0x0051 }
            java.lang.String r12 = org.tensorflow.mcr.road.RoadImage.getString(r8)     // Catch:{ Exception -> 0x0051 }
            r8 = r24
            r10 = r2
            r4.<init>(r5, r6, r7, r8, r9, r10, r11, r12)     // Catch:{ Exception -> 0x0051 }
            java.io.File r13 = r20.getSaveLocation()     // Catch:{ Exception -> 0x0051 }
            r13.mkdirs()     // Catch:{ Exception -> 0x0051 }
            java.io.File r17 = new java.io.File     // Catch:{ Exception -> 0x0051 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0051 }
            r5.<init>()     // Catch:{ Exception -> 0x0051 }
            r0 = r16
            java.lang.StringBuilder r5 = r5.append(r0)     // Catch:{ Exception -> 0x0051 }
            java.lang.String r6 = ".json"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x0051 }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x0051 }
            r0 = r17
            r0.<init>(r13, r5)     // Catch:{ Exception -> 0x0051 }
            java.io.FileWriter r15 = new java.io.FileWriter     // Catch:{ Exception -> 0x00ef }
            r0 = r17
            r15.<init>(r0)     // Catch:{ Exception -> 0x00ef }
            r6 = 0
            org.json.JSONObject r5 = r4.getJson()     // Catch:{ Throwable -> 0x00f5, all -> 0x0101 }
            java.lang.String r5 = r5.toString()     // Catch:{ Throwable -> 0x00f5, all -> 0x0101 }
            r15.write(r5)     // Catch:{ Throwable -> 0x00f5, all -> 0x0101 }
            $closeResource(r6, r15)     // Catch:{ Exception -> 0x00ef }
            goto L_0x006e
        L_0x00ef:
            r14 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r14)     // Catch:{ Exception -> 0x0051 }
            goto L_0x006e
        L_0x00f5:
            r5 = move-exception
            throw r5     // Catch:{ all -> 0x00f7 }
        L_0x00f7:
            r6 = move-exception
            r19 = r6
            r6 = r5
            r5 = r19
        L_0x00fd:
            $closeResource(r6, r15)     // Catch:{ Exception -> 0x00ef }
            throw r5     // Catch:{ Exception -> 0x00ef }
        L_0x0101:
            r5 = move-exception
            goto L_0x00fd
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.RoadDamageDetectorActivity.saveDamageData(java.util.List, org.tensorflow.mcr.road.ConvImage, java.util.Date, android.location.Location):void");
    }

    private static /* synthetic */ void $closeResource(Throwable x0, FileWriter x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                ThrowableExtension.addSuppressed(x0, th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0093, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0094, code lost:
        r12 = r8;
        r8 = r7;
        r7 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009b, code lost:
        r7 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveLocations() {
        /*
            r13 = this;
            java.util.List<android.location.Location> r7 = r13.locations     // Catch:{ Exception -> 0x0033 }
            java.util.Iterator r3 = r7.iterator()     // Catch:{ Exception -> 0x0033 }
            org.json.JSONArray r4 = new org.json.JSONArray     // Catch:{ Exception -> 0x0033 }
            r4.<init>()     // Catch:{ Exception -> 0x0033 }
        L_0x000b:
            boolean r7 = r3.hasNext()     // Catch:{ Exception -> 0x0033 }
            if (r7 == 0) goto L_0x0051
            java.lang.Object r5 = r3.next()     // Catch:{ Exception -> 0x0033 }
            android.location.Location r5 = (android.location.Location) r5     // Catch:{ Exception -> 0x0033 }
            org.tensorflow.mcr.road.log.PhoneLocationInfo r7 = new org.tensorflow.mcr.road.log.PhoneLocationInfo     // Catch:{ Exception -> 0x0033 }
            java.lang.String r8 = r13.deviceId     // Catch:{ Exception -> 0x0033 }
            java.lang.String r9 = r13.phoneCode     // Catch:{ Exception -> 0x0033 }
            java.text.SimpleDateFormat r10 = dateFormat2     // Catch:{ Exception -> 0x0033 }
            java.util.Date r11 = r13.currentDate     // Catch:{ Exception -> 0x0033 }
            java.lang.String r10 = r10.format(r11)     // Catch:{ Exception -> 0x0033 }
            r7.<init>(r8, r9, r10, r5)     // Catch:{ Exception -> 0x0033 }
            org.json.JSONObject r7 = r7.getJson()     // Catch:{ Exception -> 0x0033 }
            r4.put(r7)     // Catch:{ Exception -> 0x0033 }
            r3.remove()     // Catch:{ Exception -> 0x0033 }
            goto L_0x000b
        L_0x0033:
            r1 = move-exception
            java.lang.String r7 = "Error"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = ""
            java.lang.StringBuilder r8 = r8.append(r9)
            java.lang.String r9 = r1.toString()
            java.lang.StringBuilder r8 = r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.e(r7, r8)
        L_0x0050:
            return
        L_0x0051:
            java.io.File r0 = r13.getSaveLocation()     // Catch:{ Exception -> 0x0033 }
            r0.mkdirs()     // Catch:{ Exception -> 0x0033 }
            java.io.File r2 = new java.io.File     // Catch:{ Exception -> 0x0033 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0033 }
            r7.<init>()     // Catch:{ Exception -> 0x0033 }
            r8 = 2131492916(0x7f0c0034, float:1.8609297E38)
            java.lang.String r8 = r13.getString(r8)     // Catch:{ Exception -> 0x0033 }
            java.lang.String r8 = r13.getFileName(r8)     // Catch:{ Exception -> 0x0033 }
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ Exception -> 0x0033 }
            java.lang.String r8 = ".json"
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ Exception -> 0x0033 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0033 }
            r2.<init>(r0, r7)     // Catch:{ Exception -> 0x0033 }
            java.io.FileWriter r6 = new java.io.FileWriter     // Catch:{ IOException -> 0x008c }
            r6.<init>(r2)     // Catch:{ IOException -> 0x008c }
            r8 = 0
            java.lang.String r7 = r4.toString()     // Catch:{ Throwable -> 0x0091, all -> 0x009b }
            r6.write(r7)     // Catch:{ Throwable -> 0x0091, all -> 0x009b }
            $closeResource(r8, r6)     // Catch:{ IOException -> 0x008c }
            goto L_0x0050
        L_0x008c:
            r1 = move-exception
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r1)     // Catch:{ Exception -> 0x0033 }
            goto L_0x0050
        L_0x0091:
            r7 = move-exception
            throw r7     // Catch:{ all -> 0x0093 }
        L_0x0093:
            r8 = move-exception
            r12 = r8
            r8 = r7
            r7 = r12
        L_0x0097:
            $closeResource(r8, r6)     // Catch:{ IOException -> 0x008c }
            throw r7     // Catch:{ IOException -> 0x008c }
        L_0x009b:
            r7 = move-exception
            goto L_0x0097
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.mcr.road.RoadDamageDetectorActivity.saveLocations():void");
    }

    /* access modifiers changed from: private */
    public String getFileName(String prefix) {
        return String.format("%s_%s", new Object[]{prefix, dateFormat1.format(this.currentDate)});
    }

    /* access modifiers changed from: private */
    public File getSaveLocation() {
        return new File(getExternalFilesDir((String) null), dateFormat2.format(this.startDate));
    }
}
