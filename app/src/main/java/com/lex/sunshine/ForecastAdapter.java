package com.lex.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lex.sunshine.db.WeatherContract;


/**
 * Created by Alex on 28/11/2016.
 */

public class ForecastAdapter extends CursorAdapter {

    private Context context;


    public ForecastAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);

        this.context = context;
    }

    /*
     * Prepare the weather high/lows for presentation
     */
    private String formatHighLows(double high, double low, Context context){
        boolean isMetric = Utility.isMetric(context);

        String highLowStr = Utility.formatTemperature(high, isMetric)
                + "/" + Utility.formatTemperature(low, isMetric);

        return highLowStr;
    }

    /*
     * This is ported from FetchWeatherTask -- but now we go straight from the cursor to string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor){
        String highAndLow = formatHighLows(cursor.getDouble(DetailActivityFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(DetailActivityFragment.COL_WEATHER_MIN_TEMP), context);

        return Utility.formatDate(cursor.getLong(DetailActivityFragment.COL_WEATHER_DATE))
                + " - " + cursor.getString(DetailActivityFragment.COL_WEATHER_DESC)
                + " - " + highAndLow;
    }

    /*
     * Remember that this views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /*
     * This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor){

        // Read weather icon from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //ToDo: Change place holder image
        ImageView iconView = (ImageView)view.findViewById(R.id.list_item_icon);
        iconView.setImageResource(R.mipmap.ic_launcher);

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView weatherDescView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        weatherDescView.setText(weatherDesc);

        boolean isMetric = Utility.isMetric(context);

        int maxTemp = cursor.getInt(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView maxTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
        maxTempView.setText(Utility.formatTemperature(maxTemp, isMetric));

        int minTemp = cursor.getInt(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView minTempView = (TextView)view.findViewById(R.id.list_item_low_textview);
        minTempView.setText(Utility.formatTemperature(minTemp, isMetric));
    }

}
