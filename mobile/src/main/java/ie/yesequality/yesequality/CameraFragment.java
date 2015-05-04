package ie.yesequality.yesequality;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import ie.yesequality.yesequality.views.CameraFragmentListener;
import ie.yesequality.yesequality.views.CameraOrientationListener;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener, Camera.PictureCallback {
    public static final String TAG = "CameraFragment";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private CameraFragmentListener listener;
    private int displayOrientation;
    private int layoutOrientation;

    private ImageView ivWaterMarkPic;

    private CameraOrientationListener orientationListener;
    private int actionBarSize;

    private static int getDegreesFromRotation(int rotation) {

        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;

            case Surface.ROTATION_90:
                return 90;


            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
            default:
                return 270;
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextureView previewView = new TextureView(getActivity());

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

//        try {
//            startCamera();
//        } catch (Exception exception) {
//            Log.e(TAG, "Can't open camera with id " + cameraId, exception);
//
//            listener.onCameraError();
//            return;
//        }
    }

    /**
     * On fragment getting paused.
     */
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        orientationListener.disable();
//
//        stopCamera();
//    }

    /**
     * Start the camera preview.
     */
    private synchronized void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception exception) {
            Log.e(TAG, "Can't start camera preview due to Exception", exception);

            listener.onCameraError();
        }
    }

    /**
     * Stop the camera preview.
     */
    private synchronized void stopCameraPreview() {
        try {
            camera.stopPreview();
        } catch (Exception exception) {
            Log.i(TAG, "Exception during stopping camera preview");
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly.
     */
    public void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int degrees = getDegreesFromRotation(getActivity().getWindowManager().getDefaultDisplay().getRotation());


        int displayOrientation;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        this.displayOrientation = displayOrientation;
        this.layoutOrientation = degrees;
        // Calculate ActionBar height
//        TypedValue tv = new TypedValue();
//        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//            this.actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
//        } else {
//            this.actionBarSize = (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
//        }

        //this.actionBarSize = ((CameraMainActivityTest)getActivity()).tbActionBar.getHeight();


        camera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters.
     */
    public void setupCamera() {
        Camera.Parameters parameters = camera.getParameters();

        Camera.Size bestPreviewSize = determineBestPreviewSize(parameters);
        Camera.Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPictureSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        try {
            camera.setParameters(parameters);

        } catch (Exception ignored) {
            if (Camera.getNumberOfCameras() >= 2) {
                try {
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "Fail to connect to camera service", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    camera = Camera.open();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "Fail to connect to camera service", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Camera.Size determineBestPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    private Camera.Size determineBestPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

        return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
    }

    protected Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
        Camera.Size bestSize = null;

        for (Camera.Size currentSize : sizes) {
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

            if (isDesiredRatio && isInBounds && isBetterSize) {
                bestSize = currentSize;
            }
        }

        if (bestSize == null) {
            listener.onCameraError();

            return sizes.get(0);
        }

        return bestSize;
    }

    /**
     * Take a picture and notify the listener once the picture is taken.
     */
    public void takePicture() {
        orientationListener.rememberOrientation();

        camera.takePicture(null, null, this);
    }


    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        // The badge is 150dp x 150dp
        Log.e("CameraActivity", "bmp2 height is: " + bmp2.getHeight());
        Log.e("CameraActivity", "bmp2 width is: " + bmp2.getWidth());
        // A lot of magic numbers in here; trial an error mostly.
        // There are two sizes of badges (changing in height)
        // These should be calculated automatically instead of hardcoding them here
        // 320 x 275 ---> 150 x 129
        // 320 x 121 ---> 150 x 57
        int widthScale = 300;
        int heightScale = 129;
        if (bmp2.getHeight() != 275) {
            heightScale = 57;
        }

        // mirror the camera snapshot to match the camera preview
        Matrix matrixBmp1 = new Matrix();
        matrixBmp1.setScale(-1, 1);
        matrixBmp1.postTranslate(bmp1.getWidth(), 0);

        // set the badge position and dimension, to match the one from the camera preview
        Matrix matrixBmp2 = new Matrix();
//        if (heightScale == 57) { // I have no idea why this correction is needed for smaller badges
//            Log.e("CameraActivity", "I am correcting the height cause bmp2 height is: " + bmp2.getHeight());
//            Log.e("CameraActivity", "old bottomsize is: " + bottomPanelSize);
//            bottomPanelSize -= 26;
//            Log.e("CameraActivity", "new bottomsize is: " + bottomPanelSize);
//        }
        // Badge has to be scaled or will be grabbed as is form resources.
        // preserve badge aspect ratio
        float badgeScaleIdx = bmp2.getWidth() / bmp2.getHeight();

        // more magic here. It "works", so leaving like that for now. Too tired for a proper solution
        matrixBmp2.postTranslate(ivWaterMarkPic.getX(), ivWaterMarkPic.getY() - (Math.round(widthScale * badgeScaleIdx) / 2));

        Bitmap scaledBadge = Bitmap.createScaledBitmap(bmp2, widthScale, Math.round(widthScale * badgeScaleIdx), true);

        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(scaledBadge, matrixBmp2, null);

        return bmOverlay;
    }

    /**
     * A picture has been taken.
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {


        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);


        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        matrix.postRotate(270);

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

        Bitmap waterMark = ((BitmapDrawable) ivWaterMarkPic.getDrawable()).getBitmap();

        bitmap = overlay(bitmap, waterMark);

        bitmap = cropBitmapToSquare(bitmap);

        listener.onPictureTaken(bitmap);
    }

    private Bitmap cropBitmapToSquare(Bitmap source) {
        Bitmap cropped;
        int h = source.getHeight();
        int w = source.getWidth();

        if (w >= h) {
            int startX = w - h - ((w - h) / 2);
            source = Bitmap.createBitmap(source, startX, 0, h, h);
        } else {
            int startY = h - w - ((h - w) / 2);
            source = Bitmap.createBitmap(source, 0, startY, w, w);
        }

        return source;
    }

//    /**
//     * On camera preview surface created.
//     *
//     * @param holder
//     */
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        this.surfaceHolder = holder;
//
//        startCameraPreview();
//    }
//
//    /**
//     * On camera preview surface changed.
//     */
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        // The interface forces us to have this method but we don't need it
//        // up to now.
//    }
//
//    /**
//     * On camera preview surface getting destroyed.
//     */
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
////        if (camera != null) {
////            camera.stopPreview();
////            camera.release();
////        }
////
////        camera =  null;
//    }


    private void startCamera() {
        if (camera != null) {
            stopCamera();
        }


        camera = Camera.open(cameraId);
        startCameraPreview();
    }

    private void stopCamera() {
        stopCameraPreview();
        camera.release();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        camera.setDisplayOrientation(90);

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters params = camera.getParameters();
        params.set("orientation", "portrait");
        Camera.Size optimalSize = getOptimalPreviewSize(params.getSupportedPreviewSizes(), getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        params.setPreviewSize(optimalSize.width, optimalSize.height);
        camera.setParameters(params);
        // start preview with new settings
        try {
            camera.setPreviewTexture(surface);
            camera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
// Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.stopPreview();
        camera.release();
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