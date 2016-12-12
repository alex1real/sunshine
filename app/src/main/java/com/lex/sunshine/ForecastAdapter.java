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

    /*************
     * Constants *
     ************/
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    /*************
     * Variables *
     ************/
    private Context context;

    /****************
     * Constructors *
     ***************/
    public ForecastAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);

        this.context = context;
    }

    /**************
     * Overriders *
     *************/
    /*
     * This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor){

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        // Read weather icon from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //ToDo: Change place holder image
        viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(weatherDesc);

        boolean isMetric = Utility.isMetric(context);

        int maxTemp = cursor.getInt(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, maxTemp, isMetric));

        int minTemp = cursor.getInt(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, minTemp, isMetric));
    }

    @Override
    public int getItemViewType(int position){
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    /*
     * Remember that this views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        // Chose the layout type
        int viewType = this.getItemViewType(cursor.getPosition());

        int layoutId = (viewType == this.VIEW_TYPE_TODAY) ? R.layout.list_item_forecast_today
                : R.layout.list_item_forecast;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*******************
     * Private Methods *
     ******************/
    /*
     * Prepare the weather high/lows for presentation
     */
    private String formatHighLows(double high, double low, Context context){
        boolean isMetric = Utility.isMetric(context);

        String highLowStr = Utility.formatTemperature(context, high, isMetric)
                + "/" + Utility.formatTemperature(context, low, isMetric);

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
}
