package ie.yesequality.yesequality;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;
    TextureView previewView;
    private Camera mCamera;
    private CameraFragmentListener listener;
    private int displayOrientation;
    private int layoutOrientation;
    private ImageView ivWaterMarkPic;
    private CameraOrientationListener orientationListener;
    private RelativeLayout rlSurfaceLayout;

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
//        try {
//            startCamera();
//        } catch (Exception exception) {
//            Log.e(TAG, "Can't open mCamera with id " + cameraId, exception);
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
     * Determine the current display orientation and rotate the mCamera preview
     * accordingly.
     */
//    public void determineDisplayOrientation() {
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        Camera.getCameraInfo(cameraId, cameraInfo);
//
//        int degrees = getDegreesFromRotation(getActivity().getWindowManager().getDefaultDisplay()
//                .getRotation());
//
//
//        int displayOrientation;
//
//        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            displayOrientation = (cameraInfo.orientation + degrees) % 360;
//            displayOrientation = (360 - displayOrientation) % 360;
//        } else {
//            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
//        }
//
//        this.displayOrientation = displayOrientation;
//        this.layoutOrientation = degrees;
//
//        mCamera.setDisplayOrientation(displayOrientation);
//    }

    /**
     * Setup the mCamera parameters.
     */
    public void setupCamera() {
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size bestPreviewSize = determineBestPreviewSize(parameters);
        Camera.Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPictureSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        try {
            mCamera.setParameters(parameters);

        } catch (Exception ignored) {
            if (Camera.getNumberOfCameras() >= 2) {
                try {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "Fail to connect to mCamera service", Toast
                            .LENGTH_SHORT).show();
                }
            } else {
                try {
                    mCamera = Camera.open();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "Fail to connect to mCamera service", Toast
                            .LENGTH_SHORT).show();
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

        mCamera.takePicture(null, null, this);
    }


    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float left, float top, int parentWidth, int
            parentHeight) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        float overlayLeft = (left / parentWidth) * bmp1.getWidth();
        float overlayTop = (top / parentHeight) * bmp1.getHeight();

        float horizontalScale = ((float) bmp2.getWidth() / parentWidth) * bmp1.getWidth();
        float verticalScale = ((float) bmp2.getHeight() / parentHeight) * bmp1.getHeight();

        bmp2 = Bitmap.createScaledBitmap(bmp2, (int) (horizontalScale), (int) (verticalScale),
                false);

        canvas.drawBitmap(bmp1, 0, 0, null);

        canvas.drawBitmap(bmp2, overlayLeft, overlayTop, null);


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

        int orientation = 90;
        //First try to open a front facing camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                orientation = cameraInfo.orientation;
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

        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        mCamera.setDisplayOrientation(Math.abs(180 - orientation));

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
        Camera.Size optimalSize = getOptimalPreviewSize(params.getSupportedPreviewSizes(),
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
        mCamera.stopPreview();
        mCamera.release();
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