package com.lex.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String forecastMsg;


    /******************
     * Public Methods *
     *****************/
    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // It's necessary to allow Menu handling
        // Setting it to true allows overriding the methods:
        //   Fragment.onCreateOptionsMenu
        //   Fragment.onOptionsItemSelected
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        //Inflate Menu resource file
        menuInflater.inflate(R.menu.detailfragment, menu);

        //Locate MenuItem with ShareActionProvider
        MenuItem menuItem = menu.findItem(R.id.action_share);

        //Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        shareActionProvider.setShareIntent(this.getShareIntent(this.forecastMsg, FORECAST_SHARE_HASHTAG));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        forecastMsg = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);

        TextView detailText = (TextView)rootView.findViewById(R.id.detailText);
        detailText.setText(forecastMsg);

        return rootView;
    }

    /*******************
     * Private Methods *
     ******************/
    private Intent getShareIntent(String shareMsg, String appHashTag){
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg + appHashTag);

        return shareIntent;
    }
}
