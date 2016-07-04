package com.lex.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final String OPEN_WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&units=metric&mode=json&cnt=7&APPID=350389f98777014acf1168ddbef077d3";

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

        //Getting the weather forecast from openweathermap.org
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        String forecastJsonStr = null;

        try{
            //Construct the URL for OpenWeatherMap query
            //Possible parameters are available at http://openweathermap.org/API#forecast
            URL url = new URL(MainActivityFragment.OPEN_WEATHER_URL);

            //Create a request to OpenWeatherMap, and open the connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            //Read the input Stream into a String
            InputStream inputStream = httpURLConnection.getInputStream();

            if(inputStream == null){
                //Nothing to do
                return null;
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while((line = bufferedReader.readLine()) != null){
                // Since it's a JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it makes debbugging a *lot* easier if your print out the completed
                // buffer for debuging
                stringBuffer.append(line + "\n");
            }

            if(stringBuffer.length() == 0){
                // Stream was empty. No point in parsing.
                return null;
            }

            forecastJsonStr = stringBuffer.toString();
        }
        catch(IOException e){
            Log.e("PlaceholderFragment", "Error ", e);
        }
        finally {
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }

            if(bufferedReader != null){
                try{
                    bufferedReader.close();
                }
                catch(final IOException e){
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }

        return rootView;
    }
}
