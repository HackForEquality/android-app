package ie.yesequality.yesequality.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import ie.yesequality.yesequality.R;

public class CameraOverlayView extends View {
    TextPaint textPaint = new TextPaint();
    Rect textRect = new Rect();

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int h = getHeight();
        int w = getWidth();
        int sq = Math.min(h, w);
        int l = (w - sq) / 2;
        int t = (h - sq) / 2;
        int r = l + sq;
        int b = t + sq;
        Rect innerRect = new Rect(l, t, r, b);
        Rect outerRect = new Rect(0, 0, w, h);


        Rect above = new Rect(outerRect.left, outerRect.top, innerRect.right, innerRect.top);
        Rect left = new Rect(outerRect.left, innerRect.top, innerRect.left, innerRect.bottom);
        Rect right = new Rect(innerRect.right, innerRect.top, outerRect.right, innerRect.bottom);
        Rect bottom = new Rect(outerRect.left, innerRect.bottom, outerRect.right, outerRect.bottom);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawRect(above, paint);
        canvas.drawRect(left, paint);
        canvas.drawRect(right, paint);
        canvas.drawRect(bottom, paint);

        if (above.height() > 0 && above.width() > 0) {
            textPaint.setAntiAlias(true);
            textPaint.setColor(getResources().getColor(R.color.green));

            textPaint.setTextAlign(Paint.Align.CENTER);

            String explanation = getContext().getString(R.string.explanation_message_one_line);

            float scale = (above.width() - 2 * getResources().getDimension(R.dimen.activity_horizontal_margin)) / textPaint.measureText(explanation);
            textPaint.setTextSize(textPaint.getTextSize() * scale);


            textPaint.getTextBounds(explanation, 0, explanation.length(), textRect);

            canvas.drawText(explanation, above.centerX(), above.centerY() + (textRect.height() / 2), textPaint);
        }

    }
    
}