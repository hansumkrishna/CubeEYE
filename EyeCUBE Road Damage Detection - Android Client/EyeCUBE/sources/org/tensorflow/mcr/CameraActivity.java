package org.tensorflow.mcr;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Size;
import android.view.KeyEvent;
import android.widget.Toast;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.nio.ByteBuffer;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.mcr.CameraConnectionFragment;
import org.ateam.eyecube.mcr.R;

public abstract class CameraActivity extends Activity implements ImageReader.OnImageAvailableListener, Camera.PreviewCallback {
    private CameraConnectionFragment camera2Fragment;
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
        super.onCreate((Bundle) null);
        getWindow().addFlags(128);
        setContentView(R.layout.mcr_activity_camera);
        setFragment();
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
        if (!this.isProcessingFrame) {
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
            }
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
                Trace.endSection();
            }
        }
    }

    public synchronized void onStart() {
        super.onStart();
    }

    public synchronized void onResume() {
        super.onResume();
        this.handlerThread = new HandlerThread("inference");
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper());
    }

    public synchronized void onPause() {
        if (!isFinishing()) {
            finish();
        }
        this.handlerThread.quitSafely();
        try {
            this.handlerThread.join();
            this.handlerThread = null;
            this.handler = null;
        } catch (InterruptedException e) {
            ThrowableExtension.printStackTrace(e);
        }
        super.onPause();
        return;
    }

    public synchronized void onStop() {
        super.onStop();
    }

    public synchronized void onDestroy() {
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public synchronized void runInBackground(Runnable r) {
        if (this.handler != null) {
            this.handler.post(r);
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
        CameraManager manager = (CameraManager) getSystemService("camera");
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = (Integer) characteristics.get(CameraCharacteristics.LENS_FACING);
                if ((facing == null || facing.intValue() != 0) && ((StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)) != null) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            ThrowableExtension.printStackTrace(e);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void setFragment() {
        String cameraId = chooseCamera();
        if (cameraId == null) {
            Toast.makeText(this, "No Camera Detected", 0).show();
            finish();
        }
        this.camera2Fragment = CameraConnectionFragment.newInstance(new CameraConnectionFragment.ConnectionCallback() {
            public void onPreviewSizeChosen(Size size, int rotation) {
                CameraActivity.this.previewHeight = size.getHeight();
                CameraActivity.this.previewWidth = size.getWidth();
                CameraActivity.this.onPreviewSizeChosen(size, rotation);
            }
        }, this, getLayoutId(), getDesiredPreviewFrameSize());
        this.camera2Fragment.setCamera(cameraId);
        getFragmentManager().beginTransaction().replace(R.id.container, this.camera2Fragment).commit();
    }

    /* access modifiers changed from: protected */
    public void fillBytes(Image.Plane[] planes, byte[][] yuvBytes2) {
        for (int i = 0; i < planes.length; i++) {
            ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes2[i] == null) {
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

    /* access modifiers changed from: protected */
    public int startRecordingVideo() {
        this.camera2Fragment.startRecordingVideo();
        return 0;
    }

    /* access modifiers changed from: protected */
    public int stopRecordingVideo() {
        this.camera2Fragment.stopRecordingVideo();
        return 0;
    }
}
