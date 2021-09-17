package com.clab.cheaboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "cheaboard.db";
    public static final String TABLE_NAME = "settings_table";
    public static final String COL_VIBRATION_FEEDBACK = "settings_use_vibration_feedback";
    public static final String COL_SOUND_FEEDBACK = "settings_use_sound_feedback";
    public static final String COL_USE_SWIPE = "settings_use_swipe_popup";
    public static final String COL_USE_AUTO_COMPLETE = "settings_use_auto_complete";
    public static final String COL_USE_NUMBER_ROW = "settings_use_number_row";
    public static final String COL_USE_AUTO_PERIOD = "settings_use_auto_period";
    public static final String COL_USE_BACKKEY_LONGPRESS = "settings_use_backkey_longpress";
    public HashMap<String, Integer> mSettingsMap = new HashMap<>();

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(";
        for (String menu : Utils.BOOLEAN_SETTINGS_MENU_LIST) {
            query = query + menu + " INTEGER DEFAULT 0,";
        }
        query = query.substring(0, query.length() - 1) + ")";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    public boolean insertBoolean(HashMap<String, Integer> map) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (String menu : Utils.BOOLEAN_SETTINGS_MENU_LIST) {
            int value = map.get(menu);
            contentValues.put(menu, value);
        }
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return res;
    }

    public boolean getSettingValue(String setting) {
        String query = "SELECT " + setting + " FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            return cursor.getInt(cursor.getColumnIndex(setting)) == 1;
        }
        return false;
    }
}