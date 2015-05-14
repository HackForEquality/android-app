package ie.yesequality.yesequality.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by rory on 13/05/15.
 */
public class BitmapUtils {

    public static Bitmap overlayBitmap(Bitmap bmp1, Bitmap bmp2, float left, float top, int parentWidth, int
            parentHeight) {
        Canvas canvas = new Canvas(bmp1);

        float overlayLeft = (left / parentWidth) * bmp1.getWidth();
        float overlayTop = (top / parentHeight) * bmp1.getHeight();

        float horizontalScale = ((float) bmp2.getWidth() / parentWidth) * bmp1.getWidth();
        float verticalScale = ((float) bmp2.getHeight() / parentHeight) * bmp1.getHeight();

        bmp2 = BitmapUtils.bitmapScaler(bmp2, (int) horizontalScale, (int) verticalScale);


        canvas.drawBitmap(bmp2, overlayLeft, overlayTop, new Paint(Paint.FILTER_BITMAP_FLAG));


        return bmp1;
    }

    public static Bitmap bitmapScaler(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() /
                2, paint);

        return scaledBitmap;
    }

    public static Bitmap cropBitmapToSquare(Bitmap source) {
        int h = source.getHeight();
        int w = source.getWidth();
        int sq = Math.min(h, w);
        int l = (w - sq) / 2;
        int t = (h - sq) / 2;
        int r = l + sq;
        int b = t + sq;
        Rect innerRect = new Rect(l, t, r, b);


        source = Bitmap.createBitmap(source, innerRect.left, innerRect.top, innerRect.width(),
                innerRect.height());

        return source;
    }
}
