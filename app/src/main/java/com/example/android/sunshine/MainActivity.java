/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.android.sunshine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.ForecastAdapter;
import com.example.android.sunshine.NetworkUtils;
import com.example.android.sunshine.R;
import com.example.android.sunshine.Weather;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.WeatherDbHelper;
import com.example.android.sunshine.sync.SunshineSyncUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int Weather_loader_ID = 1;

    private Toast mToast;

    private RecyclerView mRecyclerView;

    private ForecastAdapter mForecastAdapter;

    private ProgressBar mProgressBar;

    private TextView mEmptyTextView;

    private static String Weather_Base_Uri = "https://api.openweathermap.org/data/2.5/forecast";

    private static double latitude = 26.68;

    private static double longitude = 77.12;

    public static String temperatureType;

    private static Context context;

    private Cursor mCursor;

    private String[] MAIN_FORECAST_PROJECTION = {WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLOUMN_DESCRIPTION,
            WeatherEntry.COLOUMN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID};

    // private ArrayList<Weather> mWeatherData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        context=getApplicationContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean b = sharedPreferences.getBoolean(getString(R.string.pref_general_home_location_key), true);
        Log.v("MainActivity", "value of b is " + b);
        if (b == true) {
            setLocationCoordinatesToHome(sharedPreferences, latitude, longitude);
        }
        loadLocationCoordinatesFromPreferences(sharedPreferences);
        temperatureType = sharedPreferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default_value));

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mEmptyTextView = (TextView) findViewById(R.id.empty_view);
        mEmptyTextView.setVisibility(View.GONE);


        // COMPLETED (38) Create layoutManager, a LinearLayoutManager with VERTICAL orientation and shouldReverseLayout == false
        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // COMPLETED (41) Set the layoutManager on mRecyclerView
        mRecyclerView.setLayoutManager(layoutManager);
        // COMPLETED (42) Use setHasFixedSize(true) on mRecyclerView to designate that all items in the list will have the same size
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);
        // COMPLETED (43) set mForecastAdapter equal to a new ForecastAdapter
        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter(this,this);

        // COMPLETED (44) Use mRecyclerView.setAdapter and pass in mForecastAdapter
        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        mRecyclerView.setVisibility(View.VISIBLE);


        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        } else {

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(Weather_loader_ID, null, this);
//            loaderManager.initLoader(Current_Weather_loader_ID,null,this);

//            fetchWeatherAsyncTask task = new fetchWeatherAsyncTask();
//            task.execute(Weather_Base_Uri);
            SunshineSyncUtils.startImmediateSync(this);
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }


    private void setLocationCoordinatesToHome(SharedPreferences sharedPreferences, double latitude, double longitude) {
        sharedPreferences.edit().putString(getString(R.string.pref_general_latitude_key), "26.68").apply();
        sharedPreferences.edit().putString(getString(R.string.pref_general_longitude_key), "77.12").apply();

    }


    private void loadLocationCoordinatesFromPreferences(SharedPreferences sharedPreferences) {
        try {
            latitude = Double.parseDouble(sharedPreferences.getString(getString(R.string.pref_general_latitude_key)
                    , getString(R.string.pref_general_latitude_default)));
            longitude = Double.parseDouble(sharedPreferences.getString(getString(R.string.pref_general_longitude_key)
                    , getString(R.string.pref_general_longtude_default)));
        } catch (NumberFormatException nfe) {
            Toast.makeText(getApplicationContext(), "You need to enter valid coordinates", Toast.LENGTH_SHORT).show();
        }

    }


    /*  public class fetchWeatherAsyncTask extends AsyncTask<String, Void, ArrayList<Weather>> {


          @Override
          protected ArrayList<Weather> doInBackground(String... urls) {


              Log.v("MainActivity", "AsyncTask has started");
              if (urls.length < 1 || urls[0] == null) {
                  return null;
              }
              return NetworkUtils.fetchWeatherData(urls[0]);
          }

          @Override
          protected void onPostExecute(ArrayList<Weather> result) {


  //         for(int i=0 ; i<result.size() && i<10 ; i++){
  //             mWeatherTextView.append(result.get(i).getTemp()+"   "+result.get(i).getDate()+"   "+result.get(i).getTime()+"   "+result.get(i).getDescription()+"\n");
              mProgressBar.setVisibility(View.GONE);
              mForecastAdapter.setWeatherData(result);

          }
      }

  */
    @Override
    public void onListItemClick(Long date) {
//        if(mToast!=null){
//            mToast.cancel();
//        }
//        mToast=Toast.makeText(MainActivity.this,"Index:"+clickedItemIndex,Toast.LENGTH_SHORT);
//        mToast.show();

        Intent intent = new Intent(MainActivity.this, CurrentWeatherActivity.class);
        Uri uriForDateClicked=WeatherEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(date)).build();
        intent.setData(uriForDateClicked);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.refresh:
                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    mEmptyTextView.setVisibility(View.GONE);

                    SunshineSyncUtils.startImmediateSync(this);
                    getLoaderManager().restartLoader(Weather_loader_ID, null, this);
                } else {
                    showErrorMessage();
                }
                return true;

            case R.id.maps:
                showInMaps();
                return true;

            case R.id.settings_main_activity:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void showInMaps() {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String latitude=sharedPreferences.getString(getString(R.string.pref_general_latitude_key)
                ,getString(R.string.pref_general_latitude_default));
        String longitude=sharedPreferences.getString(getString(R.string.pref_general_longitude_key)
                ,getString(R.string.pref_general_longtude_default));

        Log.v("MainActivity","value of layt and lon is "+latitude+"-"+longitude);
        Uri geoLocation = Uri.parse("geo:"+latitude+","+longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.v("MainActivity", "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {

        String sortOrder=WeatherEntry.COLUMN_DATE+" ASC";

        return new CursorLoader(this,WeatherEntry.CONTENT_URI,MAIN_FORECAST_PROJECTION,null,null,sortOrder);
    }

    @Override
//    public void onLoadFinished(Loader<ArrayList<Weather>> loader, ArrayList<Weather> data) {
    public void onLoadFinished(Loader<Cursor> loader , Cursor data) {
            mProgressBar.setVisibility(View.GONE);
            if (mForecastAdapter != null)
                mForecastAdapter.swapCursor(data);
            if (data == null) {
                showErrorMessage();
            } else {
                showWeatherDataView();
            }
        }


    @Override
//    public void onLoaderReset(Loader<ArrayList<Weather>> loader) {
    public void onLoaderReset(Loader loader) {
        invalidateData();
    }


    private void invalidateData() {
        mForecastAdapter.swapCursor(null);
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mEmptyTextView.setText("Error in data");
        mEmptyTextView.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mEmptyTextView.setText("No Internet Connection");
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mEmptyTextView.setVisibility(View.VISIBLE);
    }


    public static void fetchData() {
        NetworkUtils.fetchWeatherData(Weather_Base_Uri, latitude, longitude,context);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_general_latitude_key)) || key.equals(getString(R.string.pref_general_longitude_key))) {
            loadLocationCoordinatesFromPreferences(sharedPreferences);
            SunshineSyncUtils.startImmediateSync(this);
        }
        if (key.equals(getString(R.string.pref_general_home_location_key))) {
            Log.v("MainActivity", "value of checkBox is " + sharedPreferences.getBoolean(key, true));
            if (sharedPreferences.getBoolean(key, true) == true) {
                setLocationCoordinatesToHome(sharedPreferences, latitude, longitude);
            }
        }
        if (key.equals(getString(R.string.pref_units_key))) {
            temperatureType = sharedPreferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default_value));
            SunshineSyncUtils.startImmediateSync(this);
        }
    }
}