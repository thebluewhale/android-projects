package com.example.mykeyboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.List;

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

//        startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS"));
//        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
//        imm.showInputMethodPicker();
//        List<InputMethodInfo> list = imm.getEnabledInputMethodList();
//        // check if our keyboard is enabled as input method
//        for (InputMethodInfo inputMethod : list) {
//            String packageName = inputMethod.getPackageName();
//            System.out.println("MYLOG | " + packageName);
//            if (packageName.equals("com.example.mykeyboard")) {
//                Toast.makeText(getApplicationContext(),"Your Keyboard Enable",Toast.LENGTH_SHORT).show();
//            }
//        }
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