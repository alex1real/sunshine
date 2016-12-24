package com.lex.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.lex.sunshine.db.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>{

    /*************
     * Constants *
     ************/
    public static final String DETAIL_URI = "URI";

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER_ID = 0;

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
                    + WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    protected static final int COL_WEATHER_ID = 0;
    protected static final int COL_WEATHER_DATE = 1;
    protected static final int COL_WEATHER_DESC = 2;
    protected static final int COL_WEATHER_MAX_TEMP = 3;
    protected static final int COL_WEATHER_MIN_TEMP = 4;
    protected static final int COL_WEATHER_HUMIDITY = 5;
    protected static final int COL_WEATHER_WIND_SPEED = 6;
    protected static final int COL_WEATHER_WIND_DEGREES = 7;
    protected static final int COL_WEATHER_PRESSURE = 8;
    protected static final int COL_WEATHER_CONDITION_ID = 9;

    /*************
     * Variables *
     ************/
    private ShareActionProvider shareActionProvider;
    private Uri uri;


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
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);

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

        //if(this.forecastMsg != null)
        //    this.shareActionProvider.setShareIntent(this.getShareIntent(this.forecastMsg, FORECAST_SHARE_HASHTAG));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if(arguments != null)
            this.uri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);

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

        if(this.uri != null){
            return new CursorLoader(getActivity(),
                    this.uri,
                    FORECAST_PROJECTION,
                    null, null, null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        View view = getView();

        long dateInMillis = cursor.getLong(DetailActivityFragment.COL_WEATHER_DATE);
        TextView weekDayView = (TextView)view.findViewById(R.id.detail_day_textview);
        weekDayView.setText(Utility.getWeekDay(dateInMillis));

        TextView dateView = (TextView)view.findViewById(R.id.detail_date_textview);
        dateView.setText(Utility.getShortDate(dateInMillis));

        Context context = getContext();

        double maxTemp = cursor.getDouble(DetailActivityFragment.COL_WEATHER_MAX_TEMP);
        TextView maxTempView = (TextView)view.findViewById(R.id.detail_high_textview);
        maxTempView.setText(Utility.formatTemperature(context, maxTemp));

        double minTemp = cursor.getDouble(DetailActivityFragment.COL_WEATHER_MIN_TEMP);
        TextView minTempView = (TextView) view.findViewById(R.id.detail_low_textview);
        minTempView.setText(Utility.formatTemperature(context, minTemp));

        int weatherId = cursor.getInt(DetailActivityFragment.COL_WEATHER_CONDITION_ID);
        int iconId = Utility.selectIcon(weatherId, Utility.COLOR_COLORFUL);
        ImageView iconView = (ImageView)view.findViewById(R.id.detail_icon);
        iconView.setImageResource(iconId);

        String description = cursor.getString(DetailActivityFragment.COL_WEATHER_DESC);
        TextView descriptionView = (TextView)view.findViewById(R.id.detail_forecast_textview);
        descriptionView.setText(description);

        double humidity = cursor.getDouble(DetailActivityFragment.COL_WEATHER_HUMIDITY);
        TextView humidityView = (TextView)view.findViewById(R.id.detail_humidity_textview);
        humidityView.setText(Utility.formatHumidity(context, humidity));

        double windSpeed = cursor.getDouble(DetailActivityFragment.COL_WEATHER_WIND_SPEED);
        double windDegrees = cursor.getDouble(DetailActivityFragment.COL_WEATHER_WIND_DEGREES);
        TextView speedView = (TextView)view.findViewById(R.id.detail_wind_textview);
        speedView.setText(Utility.formatWind(context, windSpeed, windDegrees));

        double pressure = cursor.getDouble(DetailActivityFragment.COL_WEATHER_PRESSURE);
        TextView pressureView = (TextView)view.findViewById(R.id.detail_pressure_textview);
        pressureView.setText(Utility.formatPressure(context, pressure));

        if(shareActionProvider != null){
            //shareActionProvider.setShareIntent(this.getShareIntent(this.forecastMsg,
            //        FORECAST_SHARE_HASHTAG));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationChanged(String newLocation){
        if(this.uri != null){
            long date = WeatherContract.WeatherEntry.getDateFromUri(this.uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            this.uri = updatedUri;

            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
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
        String highLowStr = Utility.formatTemperature(context, high)
                + "/" + Utility.formatTemperature(context, low);

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
