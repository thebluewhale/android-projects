package com.clab.jokey;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import java.util.HashMap;

public class SettingsMain extends AppCompatActivity {
    private SharedPreferences mSharedPreferences;
    private CustomVariables mCustomVariables;
    private DataBaseHelper mDataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("키보드 설정");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCustomVariables = new CustomVariables();
        mDataBaseHelper = new DataBaseHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataBaseHelper.deleteAll();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        HashMap<String, Integer> settings = new HashMap<>();
        for (String menu : mCustomVariables.BOOLEAN_SETTINGS_MENU_LIST) {
            boolean value = mSharedPreferences.getBoolean(menu, false);
            settings.put(menu, value ? 1 : 0);
        }
        mDataBaseHelper.insertBoolean(settings);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            if (key.equals("practice")) {
                return true;
            } else {
                return false;
            }
        }
    }
}