package org.tensorflow.mcr.road;

import android.graphics.Matrix;

public class ImageMatrixUtils {
    public static Matrix getTransformationMatrix(int srcWidth, int srcHeight, int dstWidth, int dstHeight, int applyRotation, boolean maintainAspectRatio) {
        int inWidth;
        int inHeight;
        Matrix matrix = new Matrix();
        if (applyRotation != 0) {
            matrix.postTranslate(((float) (-srcWidth)) / 2.0f, ((float) (-srcHeight)) / 2.0f);
            matrix.postRotate((float) applyRotation);
        }
        boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
        if (transpose) {
            inWidth = srcHeight;
        } else {
            inWidth = srcWidth;
        }
        if (transpose) {
            inHeight = srcWidth;
        } else {
            inHeight = srcHeight;
        }
        if (!(inWidth == dstWidth && inHeight == dstHeight)) {
            float scaleFactorX = ((float) dstWidth) / ((float) inWidth);
            float scaleFactorY = ((float) dstHeight) / ((float) inHeight);
            if (maintainAspectRatio) {
                float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }
        if (applyRotation != 0) {
            matrix.postTranslate(((float) dstWidth) / 2.0f, ((float) dstHeight) / 2.0f);
        }
        return matrix;
    }
}
