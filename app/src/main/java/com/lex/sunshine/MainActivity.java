package com.lex.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.lex.sunshine.db.WeatherContract;
import com.lex.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    /*************
     * Constants *
     ************/
    public static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String LOCATION_BASE_URI = "geo:0,0";
    private final String QUERY_LOCATION_PARAM = "q";

    /*************
     * Variables *
     ************/
    private boolean isTwoPane;
    private String location;

    /**************
     * Overriders *
     *************/
    /***********************************
     * Overriders for AppCompatActivity *
     **********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        this.location = Utility.getPreferredLocation(this);

        // Check if the load activity_main contains a place holder for the detail fragment.
        if(findViewById(R.id.weather_detail_container) != null){
            isTwoPane = true;

            //In two-pane mode, show the detail view in this activity
            if(savedInstanceState == null){
                // Generate Uri for the current day and location
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, System.currentTimeMillis());

                this.replaceDetailFragment(uri);
            }
        }
        else{
            isTwoPane = false;
        }

        ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

        ff.setUseTodayLayout(!isTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

        String sysPrefLocation = Utility.getPreferredLocation(this);

        if(!this.location.equals(sysPrefLocation)){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

            if(ff != null)
                ff.onLocationChanged();

            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAGMENT_TAG);

            if(df != null){
                df.onLocationChanged(location);
            }

            this.location = sysPrefLocation;
        }
    }

    /********************************************
     * Overriders for ForecastFragment.Callback *
     *******************************************/
    @Override
    public void onItemSelected(Uri contentUri){
        if(isTwoPane){
            // In Two Pane mode, show the detail view in this activity by adding or replacing the
            // DetailFragment using a fragment transaction.
            this.replaceDetailFragment(contentUri);
        }
        else{
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);

            startActivity(intent);
        }
    }

    /***********
     * Private *
     **********/
    // It replaces the DetailFragment using a fragment transaction.
    private void replaceDetailFragment(Uri contentUri){
        Bundle args = new Bundle();
        args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

        DetailActivityFragment detailFragment = new DetailActivityFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                .commit();
    }
}
