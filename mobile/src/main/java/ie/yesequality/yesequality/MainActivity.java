package ie.yesequality.yesequality;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements PageAdapter.PagerListener {

    @InjectView(R.id.indicator)
    protected CirclePageIndicator indicator;
    @InjectView(R.id.pager)
    protected ViewPager pager;

    private PageAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        List<Fragment> fragments = getFragmentList();
        mPageAdapter = new PageAdapter(getSupportFragmentManager(), fragments, this);

        pager.setAdapter(mPageAdapter);
        pager.setPageTransformer(true, new DepthPageTransformer());

        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                getPosition(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.lilac)));
    }


    private List<Fragment> getFragmentList() {
        List<Fragment> fragments = new ArrayList<>();
        if (((YesEqualityApplication) getApplication()).isVotingStarted() &&
                getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).getBoolean(Constants.HAS_VOTED, false)) {
            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon12,
                    R.string.information_page_thank_you_top, R.string.information_page_thank_you_bottom,
                    R.color.dark_green, "", R.color.white));
        }

        if (!((YesEqualityApplication) getApplication()).isVotingEnded()) {
            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon1,
                    R.string.information_page_string_two, R.string.information_page_string_three,
                    R.color.lilac, "", R.color.white));

            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon2,
                    R.string.information_page_string_two, R.string.information_page_string_four,
                    R.color.dark_cyan, "", R.color.white));

            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon3,
                    R.string.information_page_string_two, R.string.information_page_string_five,
                    R.color.navy, "", R.color.white));

            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon4,
                    R.string.information_page_string_two, R.string.information_page_string_six,
                    R.color.dark_lilac, "", R.color.white));

            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon5,
                    R.string.information_page_string_seven, R.string.information_page_string_eight,
                    R.color.dark_navy, "http://www.checktheregister.ie", R.color.white));

            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon9,
                    R.string.information_page_string_thirteen, R.string.information_page_string_fourteen,
                    R.color.green, "", R.color.white));
        }

        if (((YesEqualityApplication) getApplication()).isVotingStarted()) {
            fragments.add(ContributorsFragment.newInstance(R.string.contributor_text, R.color.dark_red, R.color.white, R.array.contributors_list));
            fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon13,
                    R.string.information_page_github_top, R.string.information_page_github_bottom,
                    R.color.github_grey, "https://github.com/HackForEquality/android-app", R.color.white));
        }

        fragments.add(InformationPagesFragment.newInstance(R.drawable.ic_wm_yes_color,
                R.string.information_page_string_seventeen, R.string.information_page_string_eighteen,
                R.color.white, "http://www.yesequality.ie", R.color.black));

        return fragments;
    }

    @Override
    public void getPosition(int position) {

        int colorId = R.color.green;
        if (position < getFragmentList().size()) {
            if (R.color.white != getFragmentList().get(position).getArguments().getInt("COLOR_ID")) {
                colorId = getFragmentList().get(position).getArguments().getInt("COLOR_ID");
            }
        }
        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(colorId));

        getSupportActionBar().setBackgroundDrawable(colorDrawable);

    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }


}
