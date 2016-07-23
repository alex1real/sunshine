package com.lex.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Alex on 04/07/2016.
 *                                    AsyncTask<Params, Progress, Result>*/
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private final String QUERY_PARAM = "q";
    private final String FORMAT_PARAM = "mode";
    private final String UNITS_PARAM = "units";
    private final String DAYS_PARAM = "cnt";
    private final String APPID_PARAM = "APPID";

    private final String FORMAT = "json";
    private final int NUM_DAYS = 7;

    private AsyncTaskDelegator asyncTaskDelegator;


    public FetchWeatherTask(AsyncTaskDelegator asyncTaskDelegator){
        super();

        this.asyncTaskDelegator = asyncTaskDelegator;
    }


    @Override
    protected String[] doInBackground(String... params) {

        String[] forecastList = null;

        if(params.length != 2){
            return null;
        }
        else{
            String location = params[0];
            String units = params[1];

            //Getting the weather forecast from openweathermap.org
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String forecastJsonStr = null;

            try{
                //Construct the URL for OpenWeatherMap query
                //Possible parameters are available at http://openweathermap.org/API#forecast
                Uri uri = Uri.parse(this.FORECAST_BASE_URL);
                Uri.Builder uriBuilder = uri.buildUpon();

                uriBuilder.appendQueryParameter(this.QUERY_PARAM, location);
                uriBuilder.appendQueryParameter(this.FORMAT_PARAM, this.FORMAT);
                uriBuilder.appendQueryParameter(this.UNITS_PARAM, units);
                uriBuilder.appendQueryParameter(this.DAYS_PARAM, Integer.toString(this.NUM_DAYS));
                uriBuilder.appendQueryParameter(this.APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY);

                uri = uriBuilder.build();

                URL url = new URL(uri.toString());

                Log.v(this.LOG_TAG, "uri: " + uri.toString());

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

                WeatherDataParser weatherDataParser = new WeatherDataParser(units);
                forecastList = weatherDataParser.getWeatherDataFromJson(forecastJsonStr, 7);

            }
            catch (MalformedURLException e){
                Log.e(LOG_TAG, "Error ", e);
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

            return forecastList;

        }

    }

    @Override
    //void onPostExecute (Result result)
    protected void onPostExecute(String[] results) {
        super.onPostExecute(results);

        asyncTaskDelegator.updateAsyncResult(results);
    }

}
