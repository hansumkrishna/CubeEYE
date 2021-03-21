package org.tensorflow.demo;

import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;
import java.util.List;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.ateam.eyecube.mcr.R;

public class LegacyCameraConnectionFragment extends Fragment {
    private static final Logger LOGGER = new Logger();
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private HandlerThread backgroundThread;
    /* access modifiers changed from: private */
    public Camera camera;
    /* access modifiers changed from: private */
    public Size desiredSize;
    /* access modifiers changed from: private */
    public Camera.PreviewCallback imageListener;
    private int layout;
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Camera unused = LegacyCameraConnectionFragment.this.camera = Camera.open(LegacyCameraConnectionFragment.this.getCameraId());
            try {
                Camera.Parameters parameters = LegacyCameraConnectionFragment.this.camera.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null && focusModes.contains("continuous-picture")) {
                    parameters.setFocusMode("continuous-picture");
                }
                List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
                Size[] sizes = new Size[cameraSizes.size()];
                int i = 0;
                for (Camera.Size size : cameraSizes) {
                    sizes[i] = new Size(size.width, size.height);
                    i++;
                }
                Size previewSize = CameraConnectionFragment.chooseOptimalSize(sizes, LegacyCameraConnectionFragment.this.desiredSize.getWidth(), LegacyCameraConnectionFragment.this.desiredSize.getHeight());
                parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
                LegacyCameraConnectionFragment.this.camera.setDisplayOrientation(90);
                LegacyCameraConnectionFragment.this.camera.setParameters(parameters);
                LegacyCameraConnectionFragment.this.camera.setPreviewTexture(texture);
            } catch (IOException e) {
                LegacyCameraConnectionFragment.this.camera.release();
            }
            LegacyCameraConnectionFragment.this.camera.setPreviewCallbackWithBuffer(LegacyCameraConnectionFragment.this.imageListener);
            Camera.Size s = LegacyCameraConnectionFragment.this.camera.getParameters().getPreviewSize();
            LegacyCameraConnectionFragment.this.camera.addCallbackBuffer(new byte[ImageUtils.getYUVByteSize(s.height, s.width)]);
            LegacyCameraConnectionFragment.this.textureView.setAspectRatio(s.height, s.width);
            LegacyCameraConnectionFragment.this.camera.startPreview();
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };
    /* access modifiers changed from: private */
    public AutoFitTextureView textureView;

    static {
        ORIENTATIONS.append(0, 90);
        ORIENTATIONS.append(1, 0);
        ORIENTATIONS.append(2, 270);
        ORIENTATIONS.append(3, 180);
    }

    public LegacyCameraConnectionFragment(Camera.PreviewCallback imageListener2, int layout2, Size desiredSize2) {
        this.imageListener = imageListener2;
        this.layout = layout2;
        this.desiredSize = desiredSize2;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(this.layout, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (this.textureView.isAvailable()) {
            this.camera.startPreview();
        } else {
            this.textureView.setSurfaceTextureListener(this.surfaceTextureListener);
        }
    }

    public void onPause() {
        stopCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("CameraBackground");
        this.backgroundThread.start();
    }

    private void stopBackgroundThread() {
        this.backgroundThread.quitSafely();
        try {
            this.backgroundThread.join();
            this.backgroundThread = null;
        } catch (InterruptedException e) {
            LOGGER.mo6293e(e, "Exception!", new Object[0]);
        }
    }

    /* access modifiers changed from: protected */
    public void stopCamera() {
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.setPreviewCallback((Camera.PreviewCallback) null);
            this.camera.release();
            this.camera = null;
        }
    }

    /* access modifiers changed from: private */
    public int getCameraId() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == 0) {
                return i;
            }
        }
        return -1;
    }
}
