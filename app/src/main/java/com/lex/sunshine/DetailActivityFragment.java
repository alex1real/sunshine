package com.lex.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.lex.sunshine.db.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int FORECAST_LOADER_ID = 0;

    private static final String[] FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    protected static final int COL_WEATHER_ID = 0;
    protected static final int COL_WEATHER_DATE = 1;
    protected static final int COL_WEATHER_DESC = 2;
    protected static final int COL_WEATHER_MAX_TEMP = 3;
    protected static final int COL_WEATHER_MIN_TEMP = 4;

    private String forecastMsg;
    private ShareActionProvider shareActionProvider;


    /******************
     * Public Methods *
     *****************/
    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    /***************************
     * Overriders for Fragment *
     **************************/
    @Override
    public void onActivityCreated(Bundle savedInstance){
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);

        super.onActivityCreated(savedInstance);
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
        this.shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(this.forecastMsg != null)
            this.shareActionProvider.setShareIntent(this.getShareIntent(this.forecastMsg, FORECAST_SHARE_HASHTAG));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }

    /********************************************************
     * Overriders for LoaderManager.LoaderCallbacks<Cursor> *
     *******************************************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        Intent intent = getActivity().getIntent();

        if(intent != null){
            return new CursorLoader(getActivity(),
                    intent.getData(),
                    FORECAST_PROJECTION,
                    null, null, null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TextView detailText = (TextView)getView().findViewById(R.id.detailText);
        this.forecastMsg = this.convertCursorRowToUXFormat(data);

        detailText.setText(forecastMsg);

        if(shareActionProvider != null){
            shareActionProvider.setShareIntent(this.getShareIntent(this.forecastMsg,
                    FORECAST_SHARE_HASHTAG));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /*******************
     * Private Methods *
     ******************/
    private Intent getShareIntent(String shareMsg, String appHashTag){
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg + appHashTag);

        return shareIntent;
    }

    /*
     * Prepare the weather high/lows for presentation
     */
    private String formatHighLows(double high, double low, Context context){
        boolean isMetric = Utility.isMetric(context);

        String highLowStr = Utility.formatTemperature(context, high, isMetric)
                + "/" + Utility.formatTemperature(context, low, isMetric);

        return highLowStr;
    }

    /*
     * This is ported from FetchWeatherTask -- but now we go straight from the cursor to string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor){
        if(cursor.moveToFirst()) {
            String highAndLow = formatHighLows(cursor.getDouble(DetailActivityFragment.COL_WEATHER_MAX_TEMP),
                    cursor.getDouble(DetailActivityFragment.COL_WEATHER_MIN_TEMP), getActivity());

            return Utility.formatDate(cursor.getLong(DetailActivityFragment.COL_WEATHER_DATE))
                    + " - " + cursor.getString(DetailActivityFragment.COL_WEATHER_DESC)
                    + " - " + highAndLow;
        }

        return null;
    }

}
