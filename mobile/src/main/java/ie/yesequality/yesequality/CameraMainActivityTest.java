package ie.yesequality.yesequality;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ie.yesequality.yesequality.views.CameraFragmentListener;

public class CameraMainActivityTest extends ActionBarActivity implements CameraFragmentListener {
    public static final String TAG = "CameraMainActivity";
    private static final int PICTURE_QUALITY = 100;
    @InjectView(R.id.rlSurfaceLayout)
    protected RelativeLayout rlSurfaceLayout;
    @InjectView(R.id.ivWaterMarkPic)
    protected ImageView ivWaterMarkPic;
    private boolean isFinishedPhotoCapture = false;


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

    /**
     * On activity getting created.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.surface_camera_layout_test);
        ButterKnife.inject(this);

        ivWaterMarkPic.setOnDragListener(new BadgeDragListener());

        ivWaterMarkPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(data, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);
                return true;
            }
        });


        ivWaterMarkPic.setImageResource(mVoteBadges[mSelectedBadge]);
        ivWaterMarkPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedBadge >= mVoteBadges.length - 1) {
                    mSelectedBadge = 0;
                } else {
                    mSelectedBadge++;
                }

                ivWaterMarkPic.setImageResource(mVoteBadges[mSelectedBadge]);
            }
        });


        rlSurfaceLayout.setOnDragListener(new BadgeDragListener());
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
        switch (item.getItemId()) {
            case R.id.action_info:
                Intent infoIntent = new Intent(this, MainActivity.class);
                startActivity(infoIntent);
                return true;
            case R.id.action_reminders:
                Intent reminderIntent = new Intent(this, NotificationActivity.class);
                startActivity(reminderIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private void shareIt() {

        String fname = getPhotoDirectory(this) + "/yesequal.jpg";

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

    /**
     * On fragment notifying about a non-recoverable problem with the camera.
     */
    @Override
    public void onCameraError() {
        Toast.makeText(
                this,
                "Camera error",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    /**
     * The user wants to take a picture.
     *
     * @param view
     */
    public void takePicture(View view) {
        view.setEnabled(false);

        CameraFragment fragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.camera_fragment);


        fragment.takePicture();
    }

    public void swapCamera(View view) {
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.camera_fragment);
        fragment.swapCamera();
    }

    public void swapFlash(View view) {
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.camera_fragment);
        fragment.swapFlash();
    }


//    private Bitmap overlay(Bitmap bmp, Bitmap bmpBadge) {
//        Bitmap bmOverlay = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
//        Canvas canvas = new Canvas(bmOverlay);
//

//        Matrix matrixBmp1 = new Matrix();
//        matrixBmp1.postTranslate(bmp1.getWidth(), 0);
//
//        Matrix matrixBmp2 = new Matrix();
//
//        // Badge has to be scaled or will be grabbed as is form resources.
//        // preserve badge aspect ratio
////        float badgeScaleIdx = bmp2.getWidth() / bmp2.getHeight();
//
//        // more magic here. It "works", so leaving like that for now. Too tired for a proper solution
//        matrixBmp2.postTranslate(ivWaterMarkPic.getX(), ivWaterMarkPic.getY());
//
//        canvas.drawBitmap(bmp1, matrixBmp1, null);
//        canvas.drawBitmap(bmp2, matrixBmp2, null);
//
//        canvas.drawBitmap(bmp, new Matrix(), null);
//        canvas.drawBitmap(bmpBadge, new Matrix(), null);
//        return bmOverlay;
//    }

    /**
     * A picture has been taken.
     */
    public void onPictureTaken(Bitmap bitmap) {
//        Bitmap waterMark = ((BitmapDrawable) ivWaterMarkPic.getDrawable()).getBitmap();
//
//        bitmap = overlay(bitmap, waterMark);
//
//
//        try {
//            String fname = getPhotoDirectory(this) + "/yesequal.jpg";
//
//            File ld = new File(getPhotoDirectory(this));
//            if (ld.exists()) {
//                if (!ld.isDirectory()) {
//                    finish();
//                }
//            } else {
//                ld.mkdir();
//            }
//
////            Log.d("YES", "open output stream " + fname + " : " + data.length);
//
//            OutputStream os = new FileOutputStream(fname);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//            os.write(byteArray, 0, byteArray.length);
//            os.close();
//
//
//        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "FILE NOT FOUND !", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "IO EXCEPTION", Toast.LENGTH_SHORT).show();
//        }
//
//        isFinishedPhotoCapture = true;
//        invalidateOptionsMenu();

        /////////

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                getString(R.string.app_name)
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                showSavingPictureErrorToast();
                return;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(
                mediaStorageDir.getPath() + File.separator + "yesequal_" + timeStamp + ".jpg"
        );

        try {
            FileOutputStream stream = new FileOutputStream(mediaFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, PICTURE_QUALITY, stream);
        } catch (IOException exception) {
            showSavingPictureErrorToast();

            Log.w(TAG, "IOException during saving bitmap", exception);
            return;
        }

        MediaScannerConnection.scanFile(
                this,
                new String[]{mediaFile.toString()},
                new String[]{"image/jpeg"},
                null
        );


        Intent intent = new Intent(this, PhotoActivity.class);
        intent.setData(Uri.fromFile(mediaFile));
        startActivity(intent);

//        finish();
    }

    private void showSavingPictureErrorToast() {
        Toast.makeText(this, "Error saving picture", Toast.LENGTH_SHORT).show();
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