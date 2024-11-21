package com.mk.steps.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mk.steps.data.constant.OtherProperties;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "myLogs";

    private static final String TRAINING_SCRIPT =
            "create table if not exists " + OtherProperties.TRAINING_TABLE + " ("
                    + "id integer primary key autoincrement, "
                    + "internal_code text, "
                    + "date text, "
                    + "distance integer, "
                    + "duration integer, "
                    + "type integer" + ");";

    public DBHelper(Context context) {
        super(context, OtherProperties.BASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");
        db.execSQL(TRAINING_SCRIPT);
        Log.d(TAG, "--- onCreate database finish ---");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
