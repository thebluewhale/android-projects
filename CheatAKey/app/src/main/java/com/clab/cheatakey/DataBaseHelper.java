package com.clab.cheatakey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.HashMap;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "cheatakey.db";
    public static final String TABLE_NAME = "settings_table";
    private final CustomVariables mCustomVariables = new CustomVariables();
    private final ContentValues mContentValues;

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContentValues = new ContentValues();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(";
        for (String menu : mCustomVariables.BOOLEAN_SETTINGS_MENU_LIST) {
            query = query + menu + " INTEGER DEFAULT 0,";
        }
        for (String menu : mCustomVariables.INTEGER_SETTINGS_MENU_LIST) {
            query = query + menu + " INTEGER DEFAULT 1,";
        }
        query = query.substring(0, query.length() - 1) + ")";
        db.execSQL(query);
    }

    public void insertBoolean(HashMap<String, Integer> map) {
        for (String menu : mCustomVariables.BOOLEAN_SETTINGS_MENU_LIST) {
            int value = map.get(menu);
            mContentValues.put(menu, value);
        }
    }

    public void insertInteger(HashMap<String, Integer> map) {
        for (String menu : mCustomVariables.INTEGER_SETTINGS_MENU_LIST) {
            int value = map.get(menu);
            mContentValues.put(menu, value);
        }
    }

    public boolean commitQuery() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_NAME, null, mContentValues);
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

    public boolean getBooleanSettingValue(String setting) {
        String query = "SELECT " + setting + " FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            return cursor.getInt(cursor.getColumnIndex(setting)) == 1;
        }
        return false;
    }

    public int getIntegerSettingValue(String setting) {
        String query = "SELECT " + setting + " FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            return cursor.getInt(cursor.getColumnIndex(setting));
        }
        return 1;
    }
}
