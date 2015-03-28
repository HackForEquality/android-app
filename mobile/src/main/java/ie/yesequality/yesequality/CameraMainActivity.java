package ie.yesequality.yesequality;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;


public class CameraMainActivity extends Activity implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {


    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;

    int duration = Toast.LENGTH_SHORT;
    static int pictureWidth = 0;

    ImageView selfieButton, retakeButton, shareButtonBot, shareButton, infoButton;


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


        pictureWidth = screenWidth;

        RelativeLayout surfaceLayout = (RelativeLayout)findViewById(R.id.surface_layout);
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


        selfieButton = (ImageView) findViewById(R.id.selfieButton);
        selfieButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your code here


                mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
            }
        });


        retakeButton = (ImageView) findViewById(R.id.retakeButton);
        retakeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your code here

                retakeLogic();
               // mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
            }
        });


        shareButtonBot = (ImageView) findViewById(R.id.shareButtonBotom);
        shareButtonBot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your code here
                shareIt();

                // mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
            }
        });

        shareButton = (ImageView) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your code here


                // mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
            }
        });

        infoButton = (ImageView) findViewById(R.id.moreInfoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your code here


                // mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
            }
        });





    }

    private void shareIt() {

        String fname = getPhotoDirectory(CameraMainActivity.this)+"/yesequal.jpg";

        Bitmap myfile = BitmapFactory.decodeFile(fname);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);


        OutputStream outstream;
        try {
            outstream = getContentResolver().openOutputStream(uri);
            myfile.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            outstream.close();
        } catch (Exception e) {
            System.err.println(e.toString());

        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Image"));
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



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        if (mCamera != null){
            try {

                mCamera.setPreviewDisplay(surfaceHolder);


                mCamera.startPreview();
                previewing = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {


        selfieButton.setVisibility(View.VISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);
        shareButtonBot.setVisibility(View.INVISIBLE);

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

        selfieButton.setVisibility(View.VISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);
        shareButtonBot.setVisibility(View.INVISIBLE);

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




    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Matrix matrix =  new Matrix();
        matrix.postTranslate(pictureWidth/20,(int)((pictureWidth/10) * 8.5));
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, matrix , null);
        return bmOverlay;
    }


    byte[] resizeImageAndWaterMark(byte[] input, int width, int height) {
        Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
      //  Bitmap resized = Bitmap.createScaledBitmap(original, width, height, true);


        Matrix matrix = new Matrix();

        matrix.postRotate(-90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(original,width,height,true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);


        Bitmap waterMark = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_yes_icon);

        rotatedBitmap = overlay(rotatedBitmap, waterMark);


        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);

        return blob.toByteArray();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {



            data = resizeImageAndWaterMark(data, pictureWidth, pictureWidth);


        selfieButton.setVisibility(View.INVISIBLE);
        retakeButton.setVisibility(View.VISIBLE);
        shareButtonBot.setVisibility(View.VISIBLE);


            try {

                Date now = new Date(); long nowLong = now.getTime() / 9000;
                String fname = getPhotoDirectory(CameraMainActivity.this)+"/yesequal.jpg";


                File ld = new File(getPhotoDirectory(CameraMainActivity.this));
                if (ld.exists()) {
                    if (!ld.isDirectory()){
                        CameraMainActivity.this.finish();
                    }
                } else {
                    ld.mkdir();
                }

                Log.d("YES", "open output stream "+fname +" : " +data.length);

                OutputStream os = new FileOutputStream(fname);
                os.write(data,0,data.length);
                os.close();


            } catch (FileNotFoundException e) {
                Toast.makeText(CameraMainActivity.this, "FILE NOT FOUND !", duration).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(CameraMainActivity.this, "IO EXCEPTION", duration).show();
            }
          //  camera.startPreview();

    }


   public void retakeLogic() {

       selfieButton.setVisibility(View.VISIBLE);
       retakeButton.setVisibility(View.INVISIBLE);
       shareButtonBot.setVisibility(View.INVISIBLE);

       if ( mCamera != null ) {
           mCamera.startPreview();
       }

   }



    public static String getPhotoDirectory(Context context)
    {
        //return Environment.getExternalStorageDirectory().getPath() +"/cbo-up";
        //return context.getExternalCacheDir().getPath();
        return context.getExternalFilesDir(null).getPath();
    }

    @Override
    public void onShutter() {

        Toast.makeText(CameraMainActivity.this, "Selfie Time! :)", duration).show();

    }
}
