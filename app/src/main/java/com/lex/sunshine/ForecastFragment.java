package com.lex.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment
        extends Fragment
        implements AsyncTaskDelegator<String[]>{

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> forecastArrayAdapter;

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        menuInflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int itemId = menuItem.getItemId();

        if(itemId == R.id.action_refresh){
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(this);

            //TODO Receive postal code from the user, instead of fixed as a hard code.
            fetchWeatherTask.execute("94043");

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] forecastArray = {
                "Today - Sunny - 22/12",
                "Tomorrow - Foggy - 18/10",
                "Weds - Cloudy - 19/9",
                "Thurs - Asteroids - 20/18",
                "Fri - Heavy Rain - 12/7",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 9/5",
                "Sun - Sunny - 23/14" };

        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        this.forecastArrayAdapter = new ArrayAdapter<String>(this.getActivity(),
                                                             R.layout.list_item_forecast,
                                                             R.id.list_item_forecast_textview,
                                                             weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);


        ListView listViewForecast = (ListView)rootView.findViewById(R.id.listview_forecast);

        listViewForecast.setAdapter(forecastArrayAdapter);

        return rootView;
    }


    @Override
    public void updateAsyncResult(String[] results) {
        this.refreshForecastDisplay(results);
    }

    /*******************
     * Private methods *
     ******************/
    private void refreshForecastDisplay(String[] forecastList){
        this.forecastArrayAdapter.clear();
        this.forecastArrayAdapter.addAll(forecastList);
    }
}
