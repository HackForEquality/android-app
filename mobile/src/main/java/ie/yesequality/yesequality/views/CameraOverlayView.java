package ie.yesequality.yesequality.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import ie.yesequality.yesequality.R;

public class CameraOverlayView extends View {
    Rect innerRect = new Rect();
    Rect outerRect = new Rect();
    Paint paint = new Paint();
    private Rect aboveRect;
    private Rect leftRect;
    private Rect rightRect;
    private Rect bottomRect;

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int h = getHeight();
        int w = getWidth();
        int sq = Math.min(h, w);
        int l = (w - sq) / 2;
        int t = (h - sq) / 2;
        int r = l + sq;
        int b = t + sq;
        innerRect.set(l, t, r, b);
        outerRect.set(0, 0, w, h);


        aboveRect = new Rect(outerRect.left, outerRect.top, innerRect.right, innerRect.top);
        leftRect = new Rect(outerRect.left, innerRect.top, innerRect.left, innerRect.bottom);
        rightRect = new Rect(innerRect.right, innerRect.top, outerRect.right, innerRect.bottom);
        bottomRect = new Rect(outerRect.left, innerRect.bottom, outerRect.right, outerRect.bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(getResources().getColor(R.color.transparent_white));
        canvas.drawRect(aboveRect, paint);
        canvas.drawRect(leftRect, paint);
        canvas.drawRect(rightRect, paint);
        canvas.drawRect(bottomRect, paint);

    }

    public Rect getInnerRect() {
        return innerRect;
    }

    public Rect getOuterRect() {
        return outerRect;
    }
}