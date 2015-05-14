package ie.yesequality.yesequality;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Activity displaying the taken photo and offering to share it with other apps.
 *
 * @author Sebastian Kaspari <sebastian@androidzeitgeist.com>
 */
public class PhotoActivity extends AppCompatActivity {
    private static final String MIME_TYPE = "image/jpeg";
    public static String ABSOLUTE_Y = "absolute y";
    @InjectView(R.id.photo)
    protected ImageView photo;
    private Uri uri;

    public static int getActionBarHeight(Resources.Theme theme, Resources resources) {
        TypedValue tv = new TypedValue();
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.getDisplayMetrics());
        }
        return 0;
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        ButterKnife.inject(this);


        uri = getIntent().getData();
        int y = getIntent().getIntExtra(ABSOLUTE_Y, -1);

        if (y > 0) {
            photo.setPadding(0, y - getStatusBarHeight(getResources()) - getActionBarHeight(getTheme(), getResources()), 0, 0);
        } else {
            photo.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
        photo.setImageURI(uri);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture, menu);

        initializeShareAction(menu.findItem(R.id.action_share));

        return super.onCreateOptionsMenu(menu);
    }

    private void initializeShareAction(MenuItem shareItem) {
        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType(MIME_TYPE);

        shareProvider.setShareIntent(shareIntent);
    }


}
