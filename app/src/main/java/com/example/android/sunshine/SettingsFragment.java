package com.example.android.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private int counter = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = preferenceScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
        Preference latitudePreference = findPreference(getString(R.string.pref_general_latitude_key));
        Preference longitudePreference = findPreference(getString(R.string.pref_general_longitude_key));
        latitudePreference.setOnPreferenceChangeListener(this);
        longitudePreference.setOnPreferenceChangeListener(this);
    }

    private void setPreferenceSummary(Preference preference, String value) {

        if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        } else if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_general_home_location_key));

        if (preference != null) {
            if (!(preference instanceof CheckBoxPreference)) {
                if (preference instanceof EditTextPreference) {
                    Log.v("Settings Fragment", "value of checkBox " + checkBoxPreference.isChecked());
                    if (checkBoxPreference.isChecked()) {
                        checkBoxPreference.setChecked(false);
                    }
                }
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Log.v("SettingsFragment","anything1111");
        if (preference.getKey().equals(getString(R.string.pref_general_latitude_key)) ||
                preference.getKey().equals(getString(R.string.pref_general_longitude_key))) {
            Log.v("SettingsFragment","anything");
            String newString = (String) newValue;
            try {
                double location = Float.parseFloat(newString);
            } catch (NumberFormatException nfe) {
                Log.v("SettingsFragment","NumberFormatException");
                Toast.makeText(getContext(), "You need to enter valid coordinates", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
