package ie.yesequality.yesequality;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CameraMainActivity extends Activity implements SurfaceHolder.Callback,
        Camera.ShutterCallback, Camera.PictureCallback {

    static int pictureWidth = 0;
    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    int duration = Toast.LENGTH_SHORT;
    int bottomPanelSize = 0;
    int topPanelSize = 0;

    ImageView selfieButton, retakeButton, shareButtonBot, infoButton, badge;
    RelativeLayout surfaceLayout;

    private int[] mVoteBadges = new int[]{R.drawable.ic_vote_for_me,
            R.drawable.ic_vote_for_me_color,
            R.drawable.ic_yes_im_voting,
            R.drawable.ic_yes_im_voting_color,
            R.drawable.ic_we_voting,
            R.drawable.ic_we_voting_color,
            R.drawable.ic_ta,
            R.drawable.ic_ta_color,
            R.drawable.ic_yes,
            R.drawable.ic_yes_color
    };
    private int mSelectedBadge = 0;

    public static String getPhotoDirectory(Context context) {
        return context.getExternalFilesDir(null).getPath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.surface_camera_layout);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);


        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        pictureWidth = screenWidth;

        surfaceLayout = (RelativeLayout) findViewById(R.id.surface_layout);

        LinearLayout topLayout = (LinearLayout) findViewById(R.id.top_bar);
        LayoutParams paramsTop = topLayout.getLayoutParams();
        topPanelSize = paramsTop.height;
        Log.e("CameraActivity", "Top bar height is: " + topPanelSize);
        bottomPanelSize = screenHeight - topPanelSize - screenWidth;
        Log.e("CameraActivity", "Top bottomsize is: " + bottomPanelSize);

        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottom_bar);
        LayoutParams paramsBot = bottomLayout.getLayoutParams();
        paramsBot.height = bottomPanelSize;

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        controlInflater = LayoutInflater.from(getBaseContext());

        selfieButton = (ImageView) findViewById(R.id.selfieButton);
        selfieButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.takePicture(CameraMainActivity.this, null, null, CameraMainActivity.this);
                } else {
                    Toast.makeText(getBaseContext(), "Can't connect to camera", Toast.LENGTH_SHORT).show();
                }
            }
        });


        retakeButton = (ImageView) findViewById(R.id.retakeButton);
        retakeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                retakeLogic();
            }
        });


        shareButtonBot = (ImageView) findViewById(R.id.shareButtonBotom);
        shareButtonBot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shareIt();
            }
        });


        infoButton = (ImageView) findViewById(R.id.moreInfoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CameraMainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        badge = (ImageView) findViewById(R.id.waterMarkPic);
        ViewGroup.MarginLayoutParams badgeLP = (ViewGroup.MarginLayoutParams) badge.getLayoutParams();
        badgeLP.bottomMargin = bottomPanelSize;
        badge.setLayoutParams(badgeLP);
        badge.setOnDragListener(new BadgeDragListener());

        badge.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(data, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        badge.setImageResource(mVoteBadges[mSelectedBadge]);
        badge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedBadge >= mVoteBadges.length - 1) {
                    mSelectedBadge = 0;
                } else {
                    mSelectedBadge++;
                }

                badge.setImageResource(mVoteBadges[mSelectedBadge]);
            }
        });

        surfaceLayout.setOnDragListener(new BadgeDragListener());
    }

    private void shareIt() {

        String fname = getPhotoDirectory(CameraMainActivity.this) + "/yesequal.jpg";

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

        if (mCamera != null) {
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
            try {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception ex) {
                Toast.makeText(this, "Fail to connect to camera service", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                mCamera = Camera.open();
            } catch (Exception ex) {
                Toast.makeText(this, "Fail to connect to camera service", Toast.LENGTH_SHORT).show();
            }
        }

        if (mCamera != null) {
            //nexus 6 camera upside down so I presume it's the same for all Motorola devices
            if (Build.MANUFACTURER.equalsIgnoreCase("Motorola")) {
                mCamera.setDisplayOrientation(270);
            } else {
                mCamera.setDisplayOrientation(90);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
        previewing = false;

        selfieButton.setVisibility(View.VISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);
        shareButtonBot.setVisibility(View.INVISIBLE);
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
        int widthScale = 150;
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
        if (heightScale == 57) { // I have no idea why this correction is needed for smaller badges
            Log.e("CameraActivity", "I am correcting the height cause bmp2 height is: " + bmp2.getHeight());
            Log.e("CameraActivity", "old bottomsize is: " + bottomPanelSize);
            bottomPanelSize -= 26;
            Log.e("CameraActivity", "new bottomsize is: " + bottomPanelSize);
        }
        matrixBmp2.postTranslate(badge.getX(), badge.getY());

        // Badge has to be scaled or will be grabbed as is form resources.
        // preserve badge aspect ratio
        float badgeScaleIdx = bmp2.getWidth() / bmp2.getHeight();

        Bitmap scaledBadge = Bitmap.createScaledBitmap(bmp2, widthScale, Math.round(widthScale * badgeScaleIdx), true);

        canvas.drawBitmap(bmp1, matrixBmp1, null);
        canvas.drawBitmap(scaledBadge, matrixBmp2, null);

        return bmOverlay;
    }


    byte[] resizeImageAndWaterMark(byte[] input) {
        Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);

        Matrix matrix = new Matrix();
        if (!Build.MANUFACTURER.equalsIgnoreCase("Motorola")) {
            matrix.postRotate(-90);
        } else {
            matrix.postRotate(-270);
        }

        Log.d("CameraActivity", " the size of the action bar is: " + topPanelSize);
        Log.d("CameraActivity", " the original width  is " + original.getWidth());
        Log.d("CameraActivity", " the original height is " + original.getHeight());
        Bitmap scaledBitmap = Bitmap.createBitmap(original, topPanelSize, 0,
                original.getHeight(), original.getHeight(), matrix, true);

        //TODO (jos) width and height are the same, this thing does not do anything!???
//        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        Bitmap waterMark = ((BitmapDrawable) badge.getDrawable()).getBitmap();

        scaledBitmap = overlay(scaledBitmap, waterMark);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);

        return blob.toByteArray();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        data = resizeImageAndWaterMark(data);

        selfieButton.setVisibility(View.INVISIBLE);
        retakeButton.setVisibility(View.VISIBLE);
        shareButtonBot.setVisibility(View.VISIBLE);

        try {
            String fname = getPhotoDirectory(CameraMainActivity.this) + "/yesequal.jpg";

            File ld = new File(getPhotoDirectory(CameraMainActivity.this));
            if (ld.exists()) {
                if (!ld.isDirectory()) {
                    CameraMainActivity.this.finish();
                }
            } else {
                ld.mkdir();
            }

            Log.d("YES", "open output stream " + fname + " : " + data.length);

            OutputStream os = new FileOutputStream(fname);
            os.write(data, 0, data.length);
            os.close();


        } catch (FileNotFoundException e) {
            Toast.makeText(CameraMainActivity.this, "FILE NOT FOUND !", duration).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(CameraMainActivity.this, "IO EXCEPTION", duration).show();
        }

    }


    public void retakeLogic() {
        selfieButton.setVisibility(View.VISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);
        shareButtonBot.setVisibility(View.INVISIBLE);
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    @Override
    public void onShutter() {
        Toast.makeText(CameraMainActivity.this, "Share your picture!", duration).show();
    }

    private final class BadgeDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DROP:
                    View view = (View) event.getLocalState();
                    view.setX(event.getX() - (view.getWidth() / 2));
                    view.setY(event.getY() - (view.getHeight() / 2));
                    view.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}