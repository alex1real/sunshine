package com.lex.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lex.sunshine.db.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private final int FORECAST_LOADER_ID = 100;

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
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS,
            WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    protected static final int COL_WEATHER_ID = 0;
    protected static final int COL_WEATHER_DATE = 1;
    protected static final int COL_WEATHER_DESC = 2;
    protected static final int COL_WEATHER_MAX_TEMP = 3;
    protected static final int COL_WEATHER_MIN_TEMP = 4;
    protected static final int COL_LOCATION_SETTING = 5;
    protected static final int COL_WEATHER_CONDITION_ID = 6;
    protected static final int COL_LOCATION_LAT = 7;
    protected static final int COL_LOCATION_LONG = 8;

    private ForecastAdapter forecastAdapter;
    private String defaultLocation;

    /****************
     * Constructors *
     ***************/
    public ForecastFragment() {
    }

    /******************
     * Public Methods *
     *****************/
    /***************************
     * Overriders dor Fragment *
     **************************/
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //It's necessary to allow Menu Handling. Setting it to true you can override the methods:
        //   Fragment.onCreateOptionsMenu | Fragment.onOptionsItemSelected
        // to handle menu interactions
        this.setHasOptionsMenu(true);

        this.defaultLocation = getString(R.string.pref_default_location);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        menuInflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int itemId = menuItem.getItemId();

        if(itemId == R.id.action_refresh){
            this.getWeatherForecast();

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        ListView listViewForecast = (ListView)rootView.findViewById(R.id.listview_forecast);

        listViewForecast.setAdapter(forecastAdapter);

        listViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l){
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if(cursor != null){
                    String locationSetting = Utility.getPreferredLocation(getActivity());

                    Intent intent = new Intent(getActivity(), DetailActivity.class).setData(
                            WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            )
                    );

                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    /************************************************
     * Overriders for LoaderManager.LoaderCallbacks *
     ***********************************************/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_PROJECTION,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        this.forecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        this.forecastAdapter.swapCursor(null);
    }

    public void onLocationChanged(){
        this.getWeatherForecast();

        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    /*******************
     * Private methods *
     ******************/
    private void getWeatherForecast(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getContext());

        //Retrieving the location from a SharedPreference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(getString(R.string.pref_location_key), defaultLocation);
        String unit = sharedPref.getString(getString(R.string.pref_temperature_unit_key), "");

        fetchWeatherTask.execute(location, unit);
    }
}
