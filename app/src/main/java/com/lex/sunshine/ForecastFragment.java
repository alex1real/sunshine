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
import com.lex.sunshine.service.SunshineService;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String SELECTED_KEY = "selected_position";

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

    private final int FORECAST_LOADER_ID = 100;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    /*************
     * Variables *
     ************/
    private String defaultLocation;
    private ForecastAdapter forecastAdapter;
    private ListView listViewForecast;
    private int position;
    private boolean useTodayLayout = true;

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
        // The ForecastAdapter will take data from a source and use it to populate the ListView
        // it's attached to
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        this.listViewForecast = (ListView)rootView.findViewById(R.id.listview_forecast);

        this.listViewForecast.setAdapter(forecastAdapter);

        this.listViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l){
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if(cursor != null){
                    String locationSetting = Utility.getPreferredLocation(getActivity());

                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }

                ForecastFragment.this.position = position;
            }
        });

        if(savedInstanceState != null
                && savedInstanceState.containsKey(ForecastFragment.SELECTED_KEY)){
            this.position = savedInstanceState.getInt(ForecastFragment.SELECTED_KEY);
        }

        this.forecastAdapter.setUseTodayLayout(this.useTodayLayout);

        return rootView;
    }

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

        if(this.position != ListView.INVALID_POSITION){
            listViewForecast.smoothScrollToPosition(this.position);

            // Select the first item from the list
            listViewForecast.setItemChecked(0, true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        this.forecastAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        // When tablet rotates, the currently selected list item needs to be saved.
        // When no item is selected, this.position will be set to ListView.INVALID_POSITION,
        // so check that before storing.
        if(this.position != ListView.INVALID_POSITION){
            bundle.putInt(ForecastFragment.SELECTED_KEY, this.position);
        }

        super.onSaveInstanceState(bundle);
    }

    public void onLocationChanged(){
        this.getWeatherForecast();

        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout){
        this.useTodayLayout = useTodayLayout;

        if(forecastAdapter != null){
            forecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    /*******************
     * Private methods *
     ******************/
    private void getWeatherForecast(){
        String location = Utility.getPreferredLocation(getActivity());

        Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, location);
        getActivity().startService(intent);
    }

    /**************
     * Interfaces *
     *************/
    /*
     * A callback interface that all Activities containing this fragment must implement. This
     * mechanism allows activities to be notified of item selections.
     */
    public interface Callback{
        /*
         * DetailFragmantCallback for when an item has been selected
         */
        public void onItemSelected(Uri dateUri);
    }

}
