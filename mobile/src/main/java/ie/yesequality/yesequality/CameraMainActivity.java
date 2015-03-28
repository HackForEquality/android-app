package ie.yesequality.yesequality;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class CameraMainActivity extends Activity implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {


    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.surface_camera_layout);

     //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.surface_camera);



        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();


        LinearLayout surfaceLayout = (LinearLayout)findViewById(R.id.surface_layout);
        LayoutParams params = surfaceLayout.getLayoutParams();
        params.height = screenWidth;
        params.width = screenWidth;

        int usable = screenHeight - screenWidth;







        LinearLayout topLayout = (LinearLayout)findViewById(R.id.top_bar);
        LayoutParams paramsTop = topLayout.getLayoutParams();
       // paramsTop.height = usable/3;
       // paramsTop.width = usable/2;

        LinearLayout bottomLayout = (LinearLayout)findViewById(R.id.bottom_bar);
        LayoutParams paramsBot = bottomLayout.getLayoutParams();
        paramsBot.height = usable - paramsTop.height;
       // paramsBot.width = usable/2;



        //  surfaceLayout.setLayoutParams(params);

        //Get the width of the screen
      //  int screenWidth = getWindowManager().getDefaultDisplay().getWidth();




        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);




        controlInflater = LayoutInflater.from(getBaseContext());
       // View viewControl = controlInflater.inflate(R.layout.custom_camera, null);
        ActionBar.LayoutParams layoutParamsControl = new ActionBar.LayoutParams(ActionBar.LayoutParams.FILL_PARENT,
                ActionBar.LayoutParams.FILL_PARENT);
       // this.addContentView(viewControl, layoutParamsControl);


        ImageView img = (ImageView) findViewById(R.id.selfieButton);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your code here


                mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


  /*  @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if(previewing){
            mCamera.stopPreview();
            previewing = false;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(4); //480h x 720w

        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        mCamera.setParameters(parameters);

        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if(display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        } else if(display.getRotation() == Surface.ROTATION_270) {
            mCamera.setDisplayOrientation(180);
        }

        mCamera.startPreview();
    }*/


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        if (mCamera != null){
            try {

                mCamera.setPreviewDisplay(surfaceHolder);
            /*    Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
                Camera.Size previewSize = previewSizes.get(4); //480h x 720w

                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);



                mCamera.setParameters(parameters);*/

                mCamera.startPreview();
                previewing = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (Camera.getNumberOfCameras() >= 2) {

            //if you want to open front facing camera use this line
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

            //if you want to use the back facing camera
           // camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            mCamera = Camera.open();
        }


        mCamera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        previewing = false;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

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


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
            try {
                FileOutputStream out = openFileOutput("yesForEquaity.jpg", Activity.MODE_PRIVATE);
                out.write(data);
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();

    }

    @Override
    public void onShutter() {
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(CameraMainActivity.this, "Selfie Time! :)", duration).show();

    }
}
