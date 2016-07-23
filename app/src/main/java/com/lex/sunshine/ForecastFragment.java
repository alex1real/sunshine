package com.lex.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

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
            fetchWeatherTask.execute("dublin,ie");

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] forecastArray = {
                "Go to menu and tap Refresh",
                " - " };

        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        this.forecastArrayAdapter = new ArrayAdapter<String>(this.getActivity(),
                                                             R.layout.list_item_forecast,
                                                             R.id.list_item_forecast_textview,
                                                             weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);


        ListView listViewForecast = (ListView)rootView.findViewById(R.id.listview_forecast);

        listViewForecast.setAdapter(forecastArrayAdapter);

        listViewForecast.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent,
                                        View view,
                                        int position,
                                        long id){
                    //Retreiving the forecast String from the View's Adapter.
                    ArrayAdapter<String> viewArrayAdapter = (ArrayAdapter<String>)parent.getAdapter();
                    String forecastMsg = viewArrayAdapter.getItem(position);

                    //Displaying the forecast in another activity
                    Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class);
                    detailActivityIntent.putExtra(Intent.EXTRA_TEXT, forecastMsg);
                    startActivity(detailActivityIntent);
                }
            }
        );

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
