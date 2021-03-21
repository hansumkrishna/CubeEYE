package org.tensorflow.mcr.road;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ConvImage {
    public Bitmap bitmap;
    public Matrix frameToTransform;
    public Matrix toFrameTransform = new Matrix();

    public ConvImage(Matrix frameToTransform2, int width, int heigth) {
        this.frameToTransform = frameToTransform2;
        frameToTransform2.invert(this.toFrameTransform);
        this.bitmap = Bitmap.createBitmap(width, heigth, Bitmap.Config.ARGB_8888);
    }

    /* access modifiers changed from: package-private */
    public int getWidth() {
        return this.bitmap.getWidth();
    }

    /* access modifiers changed from: package-private */
    public int getHeight() {
        return this.bitmap.getHeight();
    }
}
