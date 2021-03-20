package org.tensorflow.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

public class AutoFitTextureView extends TextureView {
    private int ratioHeight;
    private int ratioWidth;

    public AutoFitTextureView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.ratioWidth = 0;
        this.ratioHeight = 0;
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        this.ratioWidth = width;
        this.ratioHeight = height;
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (this.ratioWidth == 0 || this.ratioHeight == 0) {
            setMeasuredDimension(width, height);
        } else if (width < (this.ratioWidth * height) / this.ratioHeight) {
            setMeasuredDimension(width, (this.ratioHeight * width) / this.ratioWidth);
        } else {
            setMeasuredDimension((this.ratioWidth * height) / this.ratioHeight, height);
        }
    }
}
