package com.example.android.sunshine.sync;

import android.content.Context;

import com.example.android.sunshine.MainActivity;

public class SunshineSyncTask {


    synchronized public static void syncWeather(Context context){
        MainActivity.fetchData();
    }


}
