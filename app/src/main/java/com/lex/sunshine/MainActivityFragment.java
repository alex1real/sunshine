package com.lex.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
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

        ArrayAdapter<String> forecastArrayAdapter =
                new ArrayAdapter<String>(this.getActivity(),
                                         R.layout.list_item_forecast,
                                         R.id.list_item_forecast_textview,
                                         weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        ListView listViewForecast = (ListView)rootView.findViewById(R.id.listview_forecast);

        listViewForecast.setAdapter(forecastArrayAdapter);

        return rootView;
    }
}
