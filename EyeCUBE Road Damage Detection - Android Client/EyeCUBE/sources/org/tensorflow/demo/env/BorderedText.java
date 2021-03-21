package org.tensorflow.demo.env;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.p000v4.view.ViewCompat;
import java.util.Iterator;
import java.util.Vector;

public class BorderedText {
    private final Paint exteriorPaint;
    private final Paint interiorPaint;
    private final float textSize;

    public BorderedText(float textSize2) {
        this(-1, ViewCompat.MEASURED_STATE_MASK, textSize2);
    }

    public BorderedText(int interiorColor, int exteriorColor, float textSize2) {
        this.interiorPaint = new Paint();
        this.interiorPaint.setTextSize(textSize2);
        this.interiorPaint.setColor(interiorColor);
        this.interiorPaint.setStyle(Paint.Style.FILL);
        this.interiorPaint.setAntiAlias(false);
        this.interiorPaint.setAlpha(255);
        this.exteriorPaint = new Paint();
        this.exteriorPaint.setTextSize(textSize2);
        this.exteriorPaint.setColor(exteriorColor);
        this.exteriorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.exteriorPaint.setStrokeWidth(textSize2 / 8.0f);
        this.exteriorPaint.setAntiAlias(false);
        this.exteriorPaint.setAlpha(255);
        this.textSize = textSize2;
    }

    public void setTypeface(Typeface typeface) {
        this.interiorPaint.setTypeface(typeface);
        this.exteriorPaint.setTypeface(typeface);
    }

    public void drawText(Canvas canvas, float posX, float posY, String text) {
        canvas.drawText(text, posX, posY, this.exteriorPaint);
        canvas.drawText(text, posX, posY, this.interiorPaint);
    }

    public void drawLines(Canvas canvas, float posX, float posY, Vector<String> lines) {
        int lineNum = 0;
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            drawText(canvas, posX, posY - (getTextSize() * ((float) ((lines.size() - lineNum) - 1))), it.next());
            lineNum++;
        }
    }

    public void setInteriorColor(int color) {
        this.interiorPaint.setColor(color);
    }

    public void setExteriorColor(int color) {
        this.exteriorPaint.setColor(color);
    }

    public float getTextSize() {
        return this.textSize;
    }

    public void setAlpha(int alpha) {
        this.interiorPaint.setAlpha(alpha);
        this.exteriorPaint.setAlpha(alpha);
    }

    public void getTextBounds(String line, int index, int count, Rect lineBounds) {
        this.interiorPaint.getTextBounds(line, index, count, lineBounds);
    }

    public void setTextAlign(Paint.Align align) {
        this.interiorPaint.setTextAlign(align);
        this.exteriorPaint.setTextAlign(align);
    }
}
