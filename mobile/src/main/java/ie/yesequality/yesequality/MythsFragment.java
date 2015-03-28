package ie.yesequality.yesequality;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MythsFragment extends Fragment {


    private PageAdapter pageAdapter;
    private ViewPager pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new MythsPageOneFragment());
        fragments.add(new MythsPageTwoFragment());

        pageAdapter = new PageAdapter(getFragmentManager(), fragments);

        pager = (ViewPager) getActivity().findViewById(R.id.viewpager);

        if (pager == null) {
            Log.d("IS PAGER NULL", "IT IS NULL");
        }


        if (pager != null) {
            pager.setAdapter(pageAdapter);
        } else {
            Log.d("IS PAGER NULL", "IT IS NULL");
        }


        View rootView = inflater.inflate(R.layout.fragment_myths, container, false);
        return rootView;
    }
}
