package org.tensorflow.mcr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.tensorflow.demo.AutoFitTextureView;
import org.tensorflow.demo.env.Logger;
import org.ateam.eyecube.mcr.R;

public class CameraConnectionFragment extends Fragment {
    static final /* synthetic */ boolean $assertionsDisabled = (!CameraConnectionFragment.class.desiredAssertionStatus());
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final Logger LOGGER = new Logger();
    private static final int MINIMUM_PREVIEW_SIZE = 320;
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private final ConnectionCallback cameraConnectionCallback;
    /* access modifiers changed from: private */
    public CameraDevice cameraDevice;
    private String cameraId;
    /* access modifiers changed from: private */
    public final Semaphore cameraOpenCloseLock = new Semaphore(1);
    private final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
        }

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        }
    };
    /* access modifiers changed from: private */
    public CameraCaptureSession captureSession;
    private final ImageReader.OnImageAvailableListener imageListener;
    private final Size inputSize;
    private final int layout;
    /* access modifiers changed from: private */
    public MediaRecorder mediaRecorder = null;
    private String nextVideoAbsolutePath;
    private ImageReader previewReader;
    private CaptureRequest previewRequest;
    private CaptureRequest.Builder previewRequestBuilder;
    private Size previewSize;
    private Integer sensorOrientation;
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        public void onOpened(CameraDevice cd) {
            CameraConnectionFragment.this.cameraOpenCloseLock.release();
            CameraDevice unused = CameraConnectionFragment.this.cameraDevice = cd;
            CameraConnectionFragment.this.createCameraPreviewSession();
        }

        public void onDisconnected(CameraDevice cd) {
            CameraConnectionFragment.this.cameraOpenCloseLock.release();
            cd.close();
            CameraDevice unused = CameraConnectionFragment.this.cameraDevice = null;
        }

        public void onError(CameraDevice cd, int error) {
            CameraConnectionFragment.this.cameraOpenCloseLock.release();
            cd.close();
            CameraDevice unused = CameraConnectionFragment.this.cameraDevice = null;
            Activity activity = CameraConnectionFragment.this.getActivity();
            if (activity != null) {
                activity.finish();
            }
        }
    };
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            CameraConnectionFragment.this.openCamera(width, height);
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            CameraConnectionFragment.this.configureTransform(width, height);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };
    private AutoFitTextureView textureView;
    private Size videoSize;

    public interface ConnectionCallback {
        void onPreviewSizeChosen(Size size, int i);
    }

    static {
        DEFAULT_ORIENTATIONS.append(0, 90);
        DEFAULT_ORIENTATIONS.append(1, 0);
        DEFAULT_ORIENTATIONS.append(2, SENSOR_ORIENTATION_INVERSE_DEGREES);
        DEFAULT_ORIENTATIONS.append(3, 180);
        INVERSE_ORIENTATIONS.append(0, SENSOR_ORIENTATION_INVERSE_DEGREES);
        INVERSE_ORIENTATIONS.append(1, 180);
        INVERSE_ORIENTATIONS.append(2, 90);
        INVERSE_ORIENTATIONS.append(3, 0);
    }

    private CameraConnectionFragment(ConnectionCallback connectionCallback, ImageReader.OnImageAvailableListener imageListener2, int layout2, Size inputSize2) {
        this.cameraConnectionCallback = connectionCallback;
        this.imageListener = imageListener2;
        this.layout = layout2;
        this.inputSize = inputSize2;
    }

    /* access modifiers changed from: private */
    public void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, text, 0).show();
                }
            });
        }
    }

    protected static Size chooseOptimalSize(Size[] choices, int width, int height) {
        int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        Size desiredSize = new Size(width, height);
        boolean exactSizeFound = false;
        List<Size> bigEnough = new ArrayList<>();
        List<Size> tooSmall = new ArrayList<>();
        for (Size option : choices) {
            if (option.equals(desiredSize)) {
                exactSizeFound = true;
            }
            if (option.getHeight() < minSize || option.getWidth() < minSize) {
                tooSmall.add(option);
            } else {
                bigEnough.add(option);
            }
        }
        LOGGER.mo6294i("Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize, new Object[0]);
        LOGGER.mo6294i("Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]", new Object[0]);
        LOGGER.mo6294i("Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]", new Object[0]);
        if (exactSizeFound) {
            LOGGER.mo6294i("Exact size match found.", new Object[0]);
            return desiredSize;
        } else if (bigEnough.size() > 0) {
            Size chosenSize = (Size) Collections.min(bigEnough, new CompareSizesByArea());
            LOGGER.mo6294i("Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight(), new Object[0]);
            return chosenSize;
        } else {
            LOGGER.mo6292e("Couldn't find any suitable preview size", new Object[0]);
            return choices[0];
        }
    }

    public static CameraConnectionFragment newInstance(ConnectionCallback callback, ImageReader.OnImageAvailableListener imageListener2, int layout2, Size inputSize2) {
        return new CameraConnectionFragment(callback, imageListener2, layout2, inputSize2);
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
            openCamera(this.textureView.getWidth(), this.textureView.getHeight());
        } else {
            this.textureView.setSurfaceTextureListener(this.surfaceTextureListener);
        }
    }

    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    public void setCamera(String cameraId2) {
        this.cameraId = cameraId2;
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() <= (size.getHeight() * 4) / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    private void setUpCameraOutputs() {
        try {
            CameraCharacteristics characteristics = ((CameraManager) getActivity().getSystemService("camera")).getCameraCharacteristics(this.cameraId);
            StreamConfigurationMap map = (StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size size = (Size) Collections.max(Arrays.asList(map.getOutputSizes(35)), new CompareSizesByArea());
            this.sensorOrientation = (Integer) characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            this.previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), this.inputSize.getWidth(), this.inputSize.getHeight());
            this.videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            this.mediaRecorder = new MediaRecorder();
            if (getResources().getConfiguration().orientation == 2) {
                this.textureView.setAspectRatio(this.previewSize.getWidth(), this.previewSize.getHeight());
            } else {
                this.textureView.setAspectRatio(this.previewSize.getHeight(), this.previewSize.getWidth());
            }
        } catch (CameraAccessException e) {
            LOGGER.mo6293e(e, "Exception!", new Object[0]);
        } catch (NullPointerException e2) {
            ErrorDialog.newInstance(getString(R.string.camera_error)).show(getChildFragmentManager(), FRAGMENT_DIALOG);
            throw new RuntimeException(getString(R.string.camera_error));
        }
        this.cameraConnectionCallback.onPreviewSizeChosen(this.previewSize, this.sensorOrientation.intValue());
    }

    /* access modifiers changed from: private */
    public void openCamera(int width, int height) {
        setUpCameraOutputs();
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getActivity().getSystemService("camera");
        try {
            if (!this.cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(this.cameraId, this.stateCallback, this.backgroundHandler);
        } catch (CameraAccessException e) {
            LOGGER.mo6293e(e, "Exception!", new Object[0]);
        } catch (InterruptedException e2) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e2);
        }
    }

    private void closeCamera() {
        try {
            this.cameraOpenCloseLock.acquire();
            closePreviewSession();
            if (this.cameraDevice != null) {
                this.cameraDevice.close();
                this.cameraDevice = null;
            }
            if (this.mediaRecorder != null) {
                this.mediaRecorder.release();
                this.mediaRecorder = null;
            }
            this.cameraOpenCloseLock.release();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } catch (Throwable th) {
            this.cameraOpenCloseLock.release();
            throw th;
        }
    }

    private void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("ImageListener");
        this.backgroundThread.start();
        this.backgroundHandler = new Handler(this.backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        this.backgroundThread.quitSafely();
        try {
            this.backgroundThread.join();
            this.backgroundThread = null;
            this.backgroundHandler = null;
        } catch (InterruptedException e) {
            LOGGER.mo6293e(e, "Exception!", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public void createCameraPreviewSession() {
        if (this.cameraDevice != null && this.textureView.isAvailable() && this.previewSize != null) {
            try {
                closePreviewSession();
                SurfaceTexture texture = this.textureView.getSurfaceTexture();
                if ($assertionsDisabled || texture != null) {
                    texture.setDefaultBufferSize(this.previewSize.getWidth(), this.previewSize.getHeight());
                    Surface surface = new Surface(texture);
                    this.previewRequestBuilder = this.cameraDevice.createCaptureRequest(1);
                    this.previewRequestBuilder.addTarget(surface);
                    this.previewReader = ImageReader.newInstance(this.previewSize.getWidth(), this.previewSize.getHeight(), 35, 2);
                    this.previewReader.setOnImageAvailableListener(this.imageListener, this.backgroundHandler);
                    this.previewRequestBuilder.addTarget(this.previewReader.getSurface());
                    this.cameraDevice.createCaptureSession(Arrays.asList(new Surface[]{surface, this.previewReader.getSurface()}), new CameraCaptureSession.StateCallback() {
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            CameraCaptureSession unused = CameraConnectionFragment.this.captureSession = cameraCaptureSession;
                            CameraConnectionFragment.this.updatePreview();
                        }

                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            CameraConnectionFragment.this.showToast("Failed");
                        }
                    }, (Handler) null);
                    return;
                }
                throw new AssertionError();
            } catch (CameraAccessException e) {
                LOGGER.mo6293e(e, "Exception!", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (this.textureView != null && this.previewSize != null && activity != null) {
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            Matrix matrix = new Matrix();
            RectF viewRect = new RectF(0.0f, 0.0f, (float) viewWidth, (float) viewHeight);
            RectF bufferRect = new RectF(0.0f, 0.0f, (float) this.previewSize.getHeight(), (float) this.previewSize.getWidth());
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();
            if (1 == rotation || 3 == rotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
                float scale = Math.max(((float) viewHeight) / ((float) this.previewSize.getHeight()), ((float) viewWidth) / ((float) this.previewSize.getWidth()));
                matrix.postScale(scale, scale, centerX, centerY);
                matrix.postRotate((float) ((rotation - 2) * 90), centerX, centerY);
            } else if (2 == rotation) {
                matrix.postRotate(180.0f, centerX, centerY);
            }
            this.textureView.setTransform(matrix);
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {
        CompareSizesByArea() {
        }

        public int compare(Size lhs, Size rhs) {
            return Long.signum((((long) lhs.getWidth()) * ((long) lhs.getHeight())) - (((long) rhs.getWidth()) * ((long) rhs.getHeight())));
        }
    }

    public static class ErrorDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity).setMessage(getArguments().getString(ARG_MESSAGE)).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    activity.finish();
                }
            }).create();
        }
    }

    public void startRecordingVideo() {
        if (this.cameraDevice != null && this.textureView.isAvailable() && this.previewSize != null) {
            try {
                closePreviewSession();
                setUpMediaRecorder();
                SurfaceTexture texture = this.textureView.getSurfaceTexture();
                if ($assertionsDisabled || texture != null) {
                    texture.setDefaultBufferSize(this.previewSize.getWidth(), this.previewSize.getHeight());
                    this.previewRequestBuilder = this.cameraDevice.createCaptureRequest(3);
                    List<Surface> surfaces = new ArrayList<>();
                    Surface previewSurface = new Surface(texture);
                    surfaces.add(previewSurface);
                    this.previewRequestBuilder.addTarget(previewSurface);
                    this.previewReader = ImageReader.newInstance(this.previewSize.getWidth(), this.previewSize.getHeight(), 35, 2);
                    this.previewReader.setOnImageAvailableListener(this.imageListener, this.backgroundHandler);
                    this.previewRequestBuilder.addTarget(this.previewReader.getSurface());
                    surfaces.add(this.previewReader.getSurface());
                    Surface recorderSurface = this.mediaRecorder.getSurface();
                    surfaces.add(recorderSurface);
                    this.previewRequestBuilder.addTarget(recorderSurface);
                    this.cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            CameraCaptureSession unused = CameraConnectionFragment.this.captureSession = cameraCaptureSession;
                            CameraConnectionFragment.this.updatePreview();
                            CameraConnectionFragment.this.getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    CameraConnectionFragment.this.mediaRecorder.start();
                                }
                            });
                        }

                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Activity activity = CameraConnectionFragment.this.getActivity();
                            if (activity != null) {
                                Toast.makeText(activity, "Failed", 0).show();
                            }
                        }
                    }, (Handler) null);
                    return;
                }
                throw new AssertionError();
            } catch (CameraAccessException | IOException e) {
                ThrowableExtension.printStackTrace(e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updatePreview() {
        if (this.cameraDevice != null) {
            try {
                this.previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 4);
                this.previewRequest = this.previewRequestBuilder.build();
                this.captureSession.setRepeatingRequest(this.previewRequest, this.captureCallback, this.backgroundHandler);
            } catch (CameraAccessException e) {
                ThrowableExtension.printStackTrace(e);
            }
        }
    }

    private void closePreviewSession() {
        if (this.captureSession != null) {
            this.captureSession.close();
            this.captureSession = null;
        }
        if (this.previewReader != null) {
            this.previewReader.close();
            this.previewReader = null;
        }
    }

    public void stopRecordingVideo() {
        try {
            this.mediaRecorder.stop();
            this.mediaRecorder.reset();
        } catch (Exception e) {
            ThrowableExtension.printStackTrace(e);
        }
        this.nextVideoAbsolutePath = null;
        createCameraPreviewSession();
    }

    private void setUpMediaRecorder() throws IOException {
        Activity activity = getActivity();
        if (activity != null) {
            this.mediaRecorder.setVideoSource(2);
            this.mediaRecorder.setOutputFormat(2);
            if (this.nextVideoAbsolutePath == null || this.nextVideoAbsolutePath.isEmpty()) {
                this.nextVideoAbsolutePath = getVideoFilePath(getActivity());
            }
            this.mediaRecorder.setOutputFile(this.nextVideoAbsolutePath);
            this.mediaRecorder.setVideoEncodingBitRate(8000000);
            this.mediaRecorder.setVideoFrameRate(20);
            this.mediaRecorder.setVideoSize(this.videoSize.getWidth(), this.videoSize.getHeight());
            this.mediaRecorder.setVideoEncoder(2);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (this.sensorOrientation.intValue()) {
                case 90:
                    this.mediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                case SENSOR_ORIENTATION_INVERSE_DEGREES /*270*/:
                    this.mediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }
            this.mediaRecorder.prepare();
        }
    }

    private String getVideoFilePath(Context context) {
        File dir = context.getExternalFilesDir((String) null);
        return (dir == null ? "" : dir.getAbsolutePath() + "/") + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
    }
}
