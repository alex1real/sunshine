package com.lex.sunshine;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alex on 04/07/2016.
 */
public class FetchWeatherTask extends AsyncTask<URL, Void, String> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    @Override
    protected String doInBackground(URL... urls) {
        //Getting the weather forecast from openweathermap.org
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        String forecastJsonStr = null;

        try{
            //Construct the URL for OpenWeatherMap query
            //Possible parameters are available at http://openweathermap.org/API#forecast
            URL url = new URL(ForecastFragment.OPEN_WEATHER_URL);

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
            Log.e(LOG_TAG, "Error ", e);
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
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return forecastJsonStr;

    }

}
