package com.lex.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
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
        String highAndLow = formatHighLows(cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), context);

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE))
                + " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC)
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
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding

        TextView tv = (TextView)view;
        tv.setText(this.convertCursorRowToUXFormat(cursor));
    }

}
