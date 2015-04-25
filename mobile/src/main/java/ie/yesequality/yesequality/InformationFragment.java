package ie.yesequality.yesequality;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class InformationFragment extends Fragment {

    private PageAdapter pageAdapter;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_information, container, false);

        List<Fragment> fragments = getFragmentList();


        pageAdapter = new PageAdapter(getFragmentManager(), fragments);

        viewPager = (ViewPager) rootView.findViewById(R.id.information_viewpager);

        if (viewPager != null) {
            viewPager.setAdapter(pageAdapter);
        }

        return rootView;
    }

    private List<Fragment> getFragmentList() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon1,
                R.string.information_page_string_two, R.string.information_page_string_three,
                R.color.lilac, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon2,
                R.string.information_page_string_two, R.string.information_page_string_four, R.color.dark_cyan, "",
                R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon3,
                R.string.information_page_string_two, R.string.information_page_string_five,
                R.color.navy, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon4,
                R.string.information_page_string_two, R.string.information_page_string_six,
                R.color.dark_lilac, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon5,
                R.string.information_page_string_seven, R.string.information_page_string_eight,
                R.color.dark_navy, "http://www.checktheregister.ie", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon6,
                R.string.information_page_string_nine, R.string.information_page_string_ten,
                R.color.dark_green, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon7,
                R.string.information_page_string_nine, R.string.information_page_string_eleven,
                R.color.dark_magenta, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon8,
                R.string.information_page_string_nine, R.string.information_page_string_twelve,
                R.color.dark_red, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon9,
                R.string.information_page_string_thirteen, R.string.information_page_string_fourteen,
                R.color.green, "", R.color.white));

        fragments.add(InformationPagesFragment.newInstance(R.drawable.ic_yes_color,
                R.string.information_page_string_seventeen, R.string.information_page_string_eighteen,
                R.color.white, "http://www.yesequality.ie", R.color.black));

        return fragments;
    }
}
