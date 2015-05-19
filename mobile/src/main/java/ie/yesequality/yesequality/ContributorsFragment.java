package ie.yesequality.yesequality;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContributorsFragment extends ListFragment {

    private static final String ARG_ABOVE_TEXT = "ABOVE_TEXT";
    private static final String ARG_COLOR_ID = "COLOR_ID";
    private static final String ARG_TEXT_COLOR_ID = "TEXT_COLOR_ID";
    private static final String ARG_CONTRIBUTORS = "CONTRIBUTORS";
    @InjectView(R.id.information_above_text)
    protected TextView information_above_text;
    @InjectView(android.R.id.list)
    protected ListView list;
    private int aboveTextId;
    private String[] mContributors;
    private int textColor;
    private int colorId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContributorsFragment() {
    }

    public static ContributorsFragment newInstance(int aboveTextID, int colorID, int textColorID, int contributors) {
        ContributorsFragment fragment = new ContributorsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ABOVE_TEXT, aboveTextID);
        args.putInt(ARG_COLOR_ID, colorID);
        args.putInt(ARG_TEXT_COLOR_ID, textColorID);
        args.putInt(ARG_CONTRIBUTORS, contributors);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            aboveTextId = getArguments().getInt(ARG_ABOVE_TEXT);
            colorId = getArguments().getInt(ARG_COLOR_ID);
            textColor = getResources().getColor(getArguments().getInt(ARG_TEXT_COLOR_ID));
            mContributors = getResources().getStringArray(getArguments().getInt(ARG_CONTRIBUTORS));
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contributors, container, false);
        rootView.setBackgroundResource(colorId);
        ButterKnife.inject(this, rootView);

        information_above_text.setText(aboveTextId);
        information_above_text.setTextColor(textColor);

        list.setAdapter(new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return mContributors.length;
            }

            @Override
            public String getItem(int position) {
                return mContributors[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if (textView == null) {
                    textView = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.simple_list_item, parent, false);
                    textView.setTextColor(textColor);
                }
                textView.setText(getItem(position));

                return textView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return mContributors.length != 0;
            }
        });
        return rootView;
    }


}
