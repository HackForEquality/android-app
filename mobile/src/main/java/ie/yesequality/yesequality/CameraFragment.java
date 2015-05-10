package ie.yesequality.yesequality;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import ie.yesequality.yesequality.views.CameraFragmentListener;
import ie.yesequality.yesequality.views.CameraOrientationListener;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener,
        Camera.PictureCallback {
    public static final String TAG = "CameraFragment";

    TextureView previewView;
    private Camera mCamera;
    private CameraFragmentListener listener;
    private ImageView ivWaterMarkPic;
    private CameraOrientationListener orientationListener;
    private RelativeLayout rlSurfaceLayout;
    private Camera.Size optimalSize;
    private int mCameraId;


    /**
     * Determine the current display orientation and rotate the mCamera preview
     * accordingly.
     */
    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;

    }

    /**
     * On activity getting attached.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalArgumentException(
                    "Activity has to implement CameraFragmentListener interface"
            );
        }

        listener = (CameraFragmentListener) activity;

        orientationListener = new CameraOrientationListener(activity);
    }

    /**
     * On creating view for fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        previewView = new TextureView(getActivity());

        previewView.setSurfaceTextureListener(this);

        return previewView;
    }


    /**
     * On fragment getting resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        orientationListener.enable();

        if (previewView.getSurfaceTexture() != null && mCamera != null) {
            mCamera.startPreview();
        }
    }


    /**
     * Take a picture and notify the listener once the picture is taken.
     */
    public void takePicture() {
        orientationListener.rememberOrientation();
        if (mCamera != null) {
            mCamera.takePicture(null, null, this);
        } else {
            Toast.makeText(getActivity(), "Unable to take a picture because the camera is not connected. :(", Toast.LENGTH_LONG).show();
        }
    }


    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float left, float top, int parentWidth, int
            parentHeight) {
        //Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1
        // .getConfig());
        Canvas canvas = new Canvas(bmp1);

        float overlayLeft = (left / parentWidth) * bmp1.getWidth();
        float overlayTop = (top / parentHeight) * bmp1.getHeight();

        float horizontalScale = ((float) bmp2.getWidth() / parentWidth) * bmp1.getWidth();
        float verticalScale = ((float) bmp2.getHeight() / parentHeight) * bmp1.getHeight();

        bmp2 = bitmapScaler(bmp2, (int) horizontalScale, (int) verticalScale);

        //canvas.drawBitmap(bmp1, 0, 0, null);

        canvas.drawBitmap(bmp2, overlayLeft, overlayTop, new Paint(Paint.FILTER_BITMAP_FLAG));


        return bmp1;
    }

    private Bitmap bitmapScaler(Bitmap bitmap, int newWidth, int newHeight) {
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

    /**
     * A picture has been taken.
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        matrix.postRotate((180 + setCameraDisplayOrientation(getActivity(), mCameraId,
                mCamera)) % 360);

        bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false
        );


        ivWaterMarkPic = (ImageView) getActivity().findViewById(R.id.ivWaterMarkPic);
        rlSurfaceLayout = (RelativeLayout) getActivity().findViewById(R.id.rlSurfaceLayout);


        Bitmap waterMark = ((BitmapDrawable) ivWaterMarkPic.getDrawable()).getBitmap();

        bitmap = cropBitmapToSquare(bitmap);

        bitmap = overlay(bitmap, waterMark, ivWaterMarkPic.getX(), ivWaterMarkPic.getY(),
                rlSurfaceLayout.getWidth(), rlSurfaceLayout.getHeight());


        listener.onPictureTaken(bitmap);
    }

    private Bitmap cropBitmapToSquare(Bitmap source) {
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


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = null;

        if (Camera.getNumberOfCameras() == 0) {
            Toast.makeText(getActivity(), getString(R.string.error_unable_to_connect), Toast
                    .LENGTH_LONG).show();
            return;
        }

        mCameraId = 0;
        //First try to open a front facing camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                mCameraId = i;
                break;
            }
        }

        //There were no front facing cameras, try to find a rear-facing one
        if (mCamera == null) {
            mCamera = Camera.open();
        }

        //Ah well, who really wants a camera anyway?
        if (mCamera == null) {
            Toast.makeText(getActivity(), getString(R.string.error_unable_to_connect), Toast
                    .LENGTH_LONG).show();
            return;
        }

        mCamera.setDisplayOrientation(setCameraDisplayOrientation(getActivity(), mCameraId,
                mCamera));

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters params = mCamera.getParameters();
        params.set("orientation", "portrait");
        optimalSize = getOptimalPreviewSize(params.getSupportedPreviewSizes(),
                getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics
                        ().heightPixels);
        params.setPreviewSize(optimalSize.width, optimalSize.height);
        mCamera.setParameters(params);
        // start preview with new settings
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting mCamera preview: " + e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}