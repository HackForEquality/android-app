package ie.yesequality.yesequality;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ie.yesequality.yesequality.views.CameraFragmentListener;
import ie.yesequality.yesequality.views.CameraOverlayView;

public class CameraMainActivityTest extends AppCompatActivity implements CameraFragmentListener {
    public static final String TAG = "CameraMainActivity";
    private static final int PICTURE_QUALITY = 100;
    @InjectView(R.id.tbActionBar)
    protected Toolbar tbActionBar;
    @InjectView(R.id.rlSurfaceLayout)
    protected RelativeLayout rlSurfaceLayout;
    @InjectView(R.id.ivWaterMarkPic)
    protected ImageView ivWaterMarkPic;
    @InjectView(R.id.selfieButton)
    protected ImageView selfieButton;
    @InjectView(R.id.camera_overlay)
    protected CameraOverlayView cameraOverlayView;


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
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setContentView(R.layout.surface_camera_layout_test);
        ButterKnife.inject(this);

        tbActionBar.setTitle(R.string.app_name);
        tbActionBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_info:
                        Intent infoIntent = new Intent(CameraMainActivityTest.this, MainActivity.class);
                        startActivity(infoIntent);
                        return true;
                    case R.id.action_reminders:
                        Intent reminderIntent = new Intent(CameraMainActivityTest.this, NotificationActivity.class);
                        startActivity(reminderIntent);
                        return true;

                    default:
                        return false;

                }
            }
        });

        tbActionBar.inflateMenu(R.menu.menu_camera_main);

        final BadgeDragListener badgeDragListener = new BadgeDragListener();
        ivWaterMarkPic.setOnDragListener(badgeDragListener);


        ivWaterMarkPic.setOnTouchListener(new View.OnTouchListener() {
            float downX = 0, downY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // This filters out smaller motions to try and stop the badge from
                        // shifting around while the user is trying to click on it.
                        if (Math.abs(downX - event.getX()) + Math.abs(downY - event.getY()) > 16 * getResources().getDisplayMetrics().density) {
                            ClipData data = ClipData.newPlainText("", "");
                            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                            v.startDrag(data, shadowBuilder, v, 0);
                            v.setVisibility(View.INVISIBLE);
                            return true;
                        }

                    default:
                        return false;
                }
                return false;
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
                ivWaterMarkPic.setVisibility(View.VISIBLE);
                ivWaterMarkPic.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        //You can't run this function until the ImageView has finished laying out with the new image
                        moveWaterMarkWithinBounds();
                        ivWaterMarkPic.removeOnLayoutChangeListener(this);
                    }
                });

            }
        });


        rlSurfaceLayout.setOnDragListener(new BadgeDragListener());
        cameraOverlayView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Rect overlayInnerRect = cameraOverlayView.getInnerRect();
                rlSurfaceLayout.setLayoutParams(new FrameLayout.LayoutParams(overlayInnerRect.width(), overlayInnerRect.height(), Gravity.CENTER));

                cameraOverlayView.removeOnLayoutChangeListener(this);
            }
        });
    }


    void moveWaterMarkWithinBounds() {
        float internalX = ivWaterMarkPic.getX();
        float internalY = ivWaterMarkPic.getY();

        if (internalX < ivWaterMarkPic.getWidth() / 2) {
            ivWaterMarkPic.setX(0);
        } else if (rlSurfaceLayout.getWidth() - internalX < ivWaterMarkPic.getWidth() / 2) {
            ivWaterMarkPic.setX(rlSurfaceLayout.getWidth() - ivWaterMarkPic.getWidth());
        } /*else {
            ivWaterMarkPic.setX(internalX - (ivWaterMarkPic.getWidth() / 2));
        }*/


        if (internalY < ivWaterMarkPic.getHeight() / 2) {
            ivWaterMarkPic.setY(0);
        } else if (rlSurfaceLayout.getHeight() - internalY < ivWaterMarkPic.getHeight() / 2) {
            ivWaterMarkPic.setY(rlSurfaceLayout.getHeight() - ivWaterMarkPic.getHeight());
        } /*else {
            ivWaterMarkPic.setY(internalY - (ivWaterMarkPic.getHeight() / 2));
        }*/
    }



    @Override
    protected void onResume() {
        super.onResume();
        selfieButton.setEnabled(true);
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

    @OnClick(R.id.selfieButton)
    public void takePicture(View view) {
        view.setEnabled(false);

        CameraFragment fragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.camera_fragment);


        fragment.takePicture();
    }


    /**
     * A picture has been taken.
     */
    public void onPictureTaken(Bitmap bitmap) {
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

        float internalX = 0, internalY = 0;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_LOCATION:
                    internalX = event.getX();
                    internalY = event.getY();

                    break;

                case DragEvent.ACTION_DROP:
                    View view = (View) event.getLocalState();
                    view.setX(event.getX() - (view.getWidth() / 2));
                    view.setY(event.getY() - (view.getHeight() / 2));
                    view.setVisibility(View.VISIBLE);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    View eventView = ((View) event.getLocalState());

                    if (internalX < eventView.getWidth() / 2) {
                        eventView.setX(0);
                    } else if (rlSurfaceLayout.getWidth() - internalX < eventView.getWidth() / 2) {
                        eventView.setX(rlSurfaceLayout.getWidth() - eventView.getWidth());
                    } else {
                        eventView.setX(internalX - (eventView.getWidth() / 2));
                    }


                    if (internalY < eventView.getHeight() / 2) {
                        eventView.setY(0);
                    } else if (rlSurfaceLayout.getHeight() - internalY < eventView.getHeight() / 2) {
                        eventView.setY(rlSurfaceLayout.getHeight() - eventView.getHeight());
                    } else {
                        eventView.setY(internalY - (eventView.getHeight() / 2));
                    }

                    eventView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}