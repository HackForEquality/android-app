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
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon1, R.string.information_page_string_two, R.string.information_page_string_three, R.color.lilac));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon2, R.string.information_page_string_two, R.string.information_page_string_four, R.color.dark_cyan));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon3, R.string.information_page_string_two, R.string.information_page_string_five, R.color.navy));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon4, R.string.information_page_string_two, R.string.information_page_string_six, R.color.dark_lilac));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon5, R.string.information_page_string_seven, R.string.information_page_string_eight, R.color.dark_navy));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon6, R.string.information_page_string_nine, R.string.information_page_string_ten, R.color.dark_green));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon7, R.string.information_page_string_nine, R.string.information_page_string_eleven, R.color.dark_magenta));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon8, R.string.information_page_string_nine, R.string.information_page_string_twelve, R.color.dark_red));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon9, R.string.information_page_string_thirteen, R.string.information_page_string_fourteen, R.color.green));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon10, R.string.information_page_string_fifteen, R.string.information_page_string_sixteen, R.color.lilac));
        fragments.add(InformationPagesFragment.newInstance(R.drawable.infoicon11, R.string.information_page_string_seventeen, R.string.information_page_string_eighteen, R.color.dark_cyan));
        return fragments;
    }
}
