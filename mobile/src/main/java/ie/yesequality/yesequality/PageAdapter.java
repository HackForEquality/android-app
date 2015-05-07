package ie.yesequality.yesequality;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.List;

public class PageAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;
    private PagerListener pagerListener;

    public PageAdapter(FragmentManager fm, List<Fragment> fragments, Context context) {
        super(fm);
        this.fragments = fragments;
        pagerListener = (PagerListener) context;
    }

    @Override
    public Fragment getItem(int position) {
        pagerListener.getPosition(position);
        return this.fragments.get(position);
    }


    @Override
    public int getCount() {
        return this.fragments.size();
    }


    public interface PagerListener {
        void getPosition(int position);

    }

}