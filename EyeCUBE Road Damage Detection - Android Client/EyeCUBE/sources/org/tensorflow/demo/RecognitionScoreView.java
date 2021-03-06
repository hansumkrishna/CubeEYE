package org.tensorflow.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import java.util.List;
import org.tensorflow.demo.Classifier;

public class RecognitionScoreView extends View implements ResultsView {
    private static final float TEXT_SIZE_DIP = 24.0f;
    private final Paint bgPaint;
    private final Paint fgPaint = new Paint();
    private List<Classifier.Recognition> results;
    private final float textSizePx = TypedValue.applyDimension(1, TEXT_SIZE_DIP, getResources().getDisplayMetrics());

    public RecognitionScoreView(Context context, AttributeSet set) {
        super(context, set);
        this.fgPaint.setTextSize(this.textSizePx);
        this.bgPaint = new Paint();
        this.bgPaint.setColor(-868055564);
    }

    public void setResults(List<Classifier.Recognition> results2) {
        this.results = results2;
        postInvalidate();
    }

    public void onDraw(Canvas canvas) {
        int y = (int) (this.fgPaint.getTextSize() * 1.5f);
        canvas.drawPaint(this.bgPaint);
        if (this.results != null) {
            for (Classifier.Recognition recog : this.results) {
                canvas.drawText(recog.getTitle() + ": " + recog.getConfidence(), 10.0f, (float) y, this.fgPaint);
                y = (int) (((float) y) + (this.fgPaint.getTextSize() * 1.5f));
            }
        }
    }
}
