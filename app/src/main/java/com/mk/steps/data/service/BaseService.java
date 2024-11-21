package com.mk.steps.data.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mk.steps.data.constant.OtherProperties;
import com.mk.steps.data.database.DBHelper;
import com.mk.steps.data.entity.Training;
import com.mk.steps.data.util.Helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    final String TAG = "myLogs";

    public void onCreate() {
        super.onCreate();

        dbHelper = new DBHelper(this);
        Log.d(TAG, "BaseService onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BaseService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BaseService onDestroy");
    }

    public class LocalBinder extends Binder {
        public BaseService getService() {
            return BaseService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Training
    public long insertTraining(Training training) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowID = db.insert(OtherProperties.TRAINING_TABLE, null, getTrainingContentValues(training));
        Log.d(TAG, "note row inserted, ID = " + rowID);

        dbHelper.close();
        return rowID;
    }

    private ContentValues getTrainingContentValues(Training training) {
        ContentValues cv = new ContentValues();

        cv.put("internal_code", training.getInternalCode());
        cv.put("date", Helper.getStringDateTimeForDb(training.getDateTime()));
        cv.put("distance", training.getIntDistance());
        cv.put("duration", training.getDuration());
        cv.put("type", training.getType());

        return cv;
    }

    private List<Training> getCursorTrainings(Cursor trainingCursor) throws ParseException {
        List<Training> trainings = new ArrayList<>();

        if (trainingCursor.moveToFirst()) {
            int idColIndex = trainingCursor.getColumnIndex("id");
            int internalCodeColIndex = trainingCursor.getColumnIndex("internal_code");
            int dateColIndex = trainingCursor.getColumnIndex("date");
            int distanceColIndex = trainingCursor.getColumnIndex("distance");
            int durationColIndex = trainingCursor.getColumnIndex("duration");
            int typeColIndex = trainingCursor.getColumnIndex("type");

            do {
                Training training = new Training();
                training.setId(trainingCursor.getInt(idColIndex));
                training.setInternalCode(trainingCursor.getString(internalCodeColIndex));
                training.setDateTime(Helper.getDateTimeFromString(trainingCursor.getString(dateColIndex)));
                training.setDistanceFromInt(trainingCursor.getInt(distanceColIndex));
                training.setDuration(trainingCursor.getInt(durationColIndex));
                training.setType(trainingCursor.getInt(typeColIndex));

                trainings.add(training);
            } while (trainingCursor.moveToNext());
        } else Log.d(TAG, "trainings: 0 rows");

        return trainings;
    }

    public List<Training> getTrainings() throws ParseException {
        Log.d(TAG, "start getTrainings");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor trainingCursor = db.query(OtherProperties.TRAINING_TABLE, null, null, null, null, null, null);
        List<Training> trainings = getCursorTrainings(trainingCursor);

        trainingCursor.close();
        dbHelper.close();

        return trainings;
    }

}
