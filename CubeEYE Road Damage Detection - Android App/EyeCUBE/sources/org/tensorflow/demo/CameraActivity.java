package org.tensorflow.demo;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Size;
import android.view.KeyEvent;
import android.widget.Toast;
import java.nio.ByteBuffer;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.ateam.eyecube.mcr.R;

public abstract class CameraActivity extends Activity implements ImageReader.OnImageAvailableListener, Camera.PreviewCallback {
    private static final Logger LOGGER = new Logger();
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = "android.permission.CAMERA";
    private static final String PERMISSION_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private boolean debug = false;
    private Handler handler;
    private HandlerThread handlerThread;
    private Runnable imageConverter;
    /* access modifiers changed from: private */
    public boolean isProcessingFrame = false;
    private byte[] lastPreviewFrame;
    private Runnable postInferenceCallback;
    protected int previewHeight = 0;
    protected int previewWidth = 0;
    /* access modifiers changed from: private */
    public int[] rgbBytes = null;
    private boolean useCamera2API;
    /* access modifiers changed from: private */
    public int yRowStride;
    /* access modifiers changed from: private */
    public byte[][] yuvBytes = new byte[3][];

    /* access modifiers changed from: protected */
    public abstract Size getDesiredPreviewFrameSize();

    /* access modifiers changed from: protected */
    public abstract int getLayoutId();

    /* access modifiers changed from: protected */
    public abstract void onPreviewSizeChosen(Size size, int i);

    /* access modifiers changed from: protected */
    public abstract void processImage();

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        LOGGER.mo6290d("onCreate " + this, new Object[0]);
        super.onCreate((Bundle) null);
        getWindow().addFlags(128);
        setContentView(R.layout.activity_camera);
        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }
    }

    /* access modifiers changed from: protected */
    public int[] getRgbBytes() {
        this.imageConverter.run();
        return this.rgbBytes;
    }

    /* access modifiers changed from: protected */
    public int getLuminanceStride() {
        return this.yRowStride;
    }

    /* access modifiers changed from: protected */
    public byte[] getLuminance() {
        return this.yuvBytes[0];
    }

    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (this.isProcessingFrame) {
            LOGGER.mo6300w("Dropping frame!", new Object[0]);
            return;
        }
        try {
            if (this.rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                this.previewHeight = previewSize.height;
                this.previewWidth = previewSize.width;
                this.rgbBytes = new int[(this.previewWidth * this.previewHeight)];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
            this.isProcessingFrame = true;
            this.lastPreviewFrame = bytes;
            this.yuvBytes[0] = bytes;
            this.yRowStride = this.previewWidth;
            this.imageConverter = new Runnable() {
                public void run() {
                    ImageUtils.convertYUV420SPToARGB8888(bytes, CameraActivity.this.previewWidth, CameraActivity.this.previewHeight, CameraActivity.this.rgbBytes);
                }
            };
            this.postInferenceCallback = new Runnable() {
                public void run() {
                    camera.addCallbackBuffer(bytes);
                    boolean unused = CameraActivity.this.isProcessingFrame = false;
                }
            };
            processImage();
        } catch (Exception e) {
            LOGGER.mo6293e(e, "Exception!", new Object[0]);
        }
    }

    public void onImageAvailable(ImageReader reader) {
        if (this.previewWidth != 0 && this.previewHeight != 0) {
            if (this.rgbBytes == null) {
                this.rgbBytes = new int[(this.previewWidth * this.previewHeight)];
            }
            try {
                final Image image = reader.acquireLatestImage();
                if (image == null) {
                    return;
                }
                if (this.isProcessingFrame) {
                    image.close();
                    return;
                }
                this.isProcessingFrame = true;
                Trace.beginSection("imageAvailable");
                Image.Plane[] planes = image.getPlanes();
                fillBytes(planes, this.yuvBytes);
                this.yRowStride = planes[0].getRowStride();
                final int uvRowStride = planes[1].getRowStride();
                final int uvPixelStride = planes[1].getPixelStride();
                this.imageConverter = new Runnable() {
                    public void run() {
                        ImageUtils.convertYUV420ToARGB8888(CameraActivity.this.yuvBytes[0], CameraActivity.this.yuvBytes[1], CameraActivity.this.yuvBytes[2], CameraActivity.this.previewWidth, CameraActivity.this.previewHeight, CameraActivity.this.yRowStride, uvRowStride, uvPixelStride, CameraActivity.this.rgbBytes);
                    }
                };
                this.postInferenceCallback = new Runnable() {
                    public void run() {
                        image.close();
                        boolean unused = CameraActivity.this.isProcessingFrame = false;
                    }
                };
                processImage();
                Trace.endSection();
            } catch (Exception e) {
                LOGGER.mo6293e(e, "Exception!", new Object[0]);
                Trace.endSection();
            }
        }
    }

    public synchronized void onStart() {
        LOGGER.mo6290d("onStart " + this, new Object[0]);
        super.onStart();
    }

    public synchronized void onResume() {
        LOGGER.mo6290d("onResume " + this, new Object[0]);
        super.onResume();
        this.handlerThread = new HandlerThread("inference");
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper());
    }

    public synchronized void onPause() {
        LOGGER.mo6290d("onPause " + this, new Object[0]);
        if (!isFinishing()) {
            LOGGER.mo6290d("Requesting finish", new Object[0]);
            finish();
        }
        this.handlerThread.quitSafely();
        try {
            this.handlerThread.join();
            this.handlerThread = null;
            this.handler = null;
        } catch (InterruptedException e) {
            LOGGER.mo6293e(e, "Exception!", new Object[0]);
        }
        super.onPause();
        return;
    }

    public synchronized void onStop() {
        LOGGER.mo6290d("onStop " + this, new Object[0]);
        super.onStop();
    }

    public synchronized void onDestroy() {
        LOGGER.mo6290d("onDestroy " + this, new Object[0]);
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public synchronized void runInBackground(Runnable r) {
        if (this.handler != null) {
            this.handler.post(r);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 1) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == 0 && grantResults[1] == 0) {
            setFragment();
        } else {
            requestPermission();
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (checkSelfPermission(PERMISSION_CAMERA) == 0 && checkSelfPermission(PERMISSION_STORAGE) == 0) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(this, "Camera AND storage permission are required for this demo", 1).show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE}, 1);
        }
    }

    private boolean isHardwareLevelSupported(CameraCharacteristics characteristics, int requiredLevel) {
        boolean z = true;
        int deviceLevel = ((Integer) characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue();
        if (deviceLevel != 2) {
            if (requiredLevel > deviceLevel) {
                z = false;
            }
            return z;
        } else if (requiredLevel == deviceLevel) {
            return true;
        } else {
            return false;
        }
    }

    private String chooseCamera() {
        boolean z = true;
        CameraManager manager = (CameraManager) getSystemService("camera");
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = (Integer) characteristics.get(CameraCharacteristics.LENS_FACING);
                if ((facing == null || facing.intValue() != 0) && ((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)) != null) {
                    if (facing.intValue() != 2 && !isHardwareLevelSupported(characteristics, 1)) {
                        z = false;
                    }
                    this.useCamera2API = z;
                    LOGGER.mo6294i("Camera API lv2?: %s", Boolean.valueOf(this.useCamera2API));
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            LOGGER.mo6293e(e, "Not allowed to access camera", new Object[0]);
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: org.tensorflow.demo.LegacyCameraConnectionFragment} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: org.tensorflow.demo.LegacyCameraConnectionFragment} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: org.tensorflow.demo.CameraConnectionFragment} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: org.tensorflow.demo.LegacyCameraConnectionFragment} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFragment() {
        /*
            r6 = this;
            java.lang.String r1 = r6.chooseCamera()
            if (r1 != 0) goto L_0x0013
            java.lang.String r3 = "No Camera Detected"
            r4 = 0
            android.widget.Toast r3 = android.widget.Toast.makeText(r6, r3, r4)
            r3.show()
            r6.finish()
        L_0x0013:
            boolean r3 = r6.useCamera2API
            if (r3 == 0) goto L_0x003f
            org.tensorflow.demo.CameraActivity$5 r3 = new org.tensorflow.demo.CameraActivity$5
            r3.<init>()
            int r4 = r6.getLayoutId()
            android.util.Size r5 = r6.getDesiredPreviewFrameSize()
            org.tensorflow.demo.CameraConnectionFragment r0 = org.tensorflow.demo.CameraConnectionFragment.newInstance(r3, r6, r4, r5)
            r0.setCamera(r1)
            r2 = r0
        L_0x002c:
            android.app.FragmentManager r3 = r6.getFragmentManager()
            android.app.FragmentTransaction r3 = r3.beginTransaction()
            r4 = 2131361878(0x7f0a0056, float:1.834352E38)
            android.app.FragmentTransaction r3 = r3.replace(r4, r2)
            r3.commit()
            return
        L_0x003f:
            org.tensorflow.demo.LegacyCameraConnectionFragment r2 = new org.tensorflow.demo.LegacyCameraConnectionFragment
            int r3 = r6.getLayoutId()
            android.util.Size r4 = r6.getDesiredPreviewFrameSize()
            r2.<init>(r6, r3, r4)
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: org.tensorflow.demo.CameraActivity.setFragment():void");
    }

    /* access modifiers changed from: protected */
    public void fillBytes(Image.Plane[] planes, byte[][] yuvBytes2) {
        for (int i = 0; i < planes.length; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes2[i] == null) {
                LOGGER.mo6290d("Initializing buffer %d at size %d", Integer.valueOf(i), Integer.valueOf(buffer.capacity()));
                yuvBytes2[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes2[i]);
        }
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void requestRender() {
        OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.postInvalidate();
        }
    }

    public void addCallback(OverlayView.DrawCallback callback) {
        OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
        if (overlay != null) {
            overlay.addCallback(callback);
        }
    }

    public void onSetDebug(boolean debug2) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 25 && keyCode != 24 && keyCode != 102 && keyCode != 23) {
            return super.onKeyDown(keyCode, event);
        }
        this.debug = !this.debug;
        requestRender();
        onSetDebug(this.debug);
        return true;
    }

    /* access modifiers changed from: protected */
    public void readyForNextImage() {
        if (this.postInferenceCallback != null) {
            this.postInferenceCallback.run();
        }
    }

    /* access modifiers changed from: protected */
    public int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                return 0;
        }
    }
}
