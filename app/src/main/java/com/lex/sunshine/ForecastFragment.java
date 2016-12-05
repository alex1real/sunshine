package com.lex.sunshine;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lex.sunshine.db.WeatherContract;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment
        extends Fragment{

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

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
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        forecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        ListView listViewForecast = (ListView)rootView.findViewById(R.id.listview_forecast);

        listViewForecast.setAdapter(forecastAdapter);

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();

        this.getWeatherForecast();
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
