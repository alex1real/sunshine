package com.lex.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String LOCATION_BASE_URI = "geo:0,0";
    private final String QUERY_LOCATION_PARAM = "q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
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
        else if(id == R.id.action_view_location){
            this.viewLocationOnExternalMap();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /***********
     * Private *
     **********/
    private void viewLocationOnExternalMap(){
        //Retrieving location from Preferences
        String defaultLocation = getString(R.string.pref_default_location);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPref.getString(getString(R.string.pref_location_key), defaultLocation);

        //Building uri getActivity request
        Uri uri = Uri.parse(this.LOCATION_BASE_URI);
        Uri.Builder uriBuilder = uri.buildUpon();
        uriBuilder.appendQueryParameter(this.QUERY_LOCATION_PARAM, location);
        uri = uriBuilder.build();

        Log.v(this.LOG_TAG, "Intent Map URI: " + uri.toString());

        //Creating Intent
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(uri);

        if(mapIntent.resolveActivity(getPackageManager()) != null){
            startActivity(mapIntent);
        }
    }

}
