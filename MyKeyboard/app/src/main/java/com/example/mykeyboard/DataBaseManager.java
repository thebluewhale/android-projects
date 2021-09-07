package com.example.mykeyboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

public class DataBaseManager {

    private static DataBaseManager mDataBaseManager = null;
    private DataBaseHelper mDataBaseHelper;
    private HashMap<String, Integer> mSettingsMap = new HashMap<>();

    public static DataBaseManager getInstance(Context context) {
        if (mDataBaseManager == null) {
            mDataBaseManager = new DataBaseManager(context);
        }
        return mDataBaseManager;
    }

    private DataBaseManager(Context context) {
        initializeDataBaseHelper(context);
    }

    public boolean getSettingValue(String setting) {
        if (!mSettingsMap.containsKey(setting)) {
            return false;
        }
        return mSettingsMap.get(setting) == 1;
    }

    public Cursor getAllData() {
        return mDataBaseHelper.getAllData();
    }

    public boolean insertBoolean(HashMap<String, Integer> map) {
        return mDataBaseHelper.insertBoolean(map);
    }

    public void deleteAll() {
        mDataBaseHelper.deleteAll();;
    }

    private void initializeDataBaseHelper(Context context) {
        mDataBaseHelper = new DataBaseHelper(context);
        Cursor settings = mDataBaseHelper.getAllData();
        if (settings.getCount() != 0) {
            while (settings.moveToNext()) {
                for (int i = 0; i < settings.getColumnCount(); i++) {
                    String name = settings.getColumnName(i);
                    int value = settings.getInt(i);
                    mSettingsMap.put(name, value);
                }
            }
        }
    }
}
