package com.mk.steps.data.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mk.steps.data.DBHelper;
import com.mk.steps.data.entity.Training;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    private static final String BASE_NAME = "my_music.db";
    private static final String TRAINING_TABLE = "training";

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

    // Note
    public void insertTraining(Training training) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowID = db.insert(TRAINING_TABLE, null, getTrainingContentValues(training));
        Log.d(TAG, "note row inserted, ID = " + rowID);

        dbHelper.close();
    }

    private ContentValues getTrainingContentValues(Training training) {
        ContentValues cv = new ContentValues();

        String date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-LL-dd");
            date = formatter.format(training.getDate());
        }

        cv.put("date", date);
        cv.put("distance", training.getDistance());
        cv.put("duration", training.getDuration());
        cv.put("type", training.getType());

        return cv;
    }

//    public void updateNote(Note note) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        int updCount = db.update("note", getTrainingContentValues(note), "id = " + note.getId(), null);
//        Log.d(TAG, "note updated rows count = " + updCount);
//        dbHelper.close();
//    }

//    public void deleteNote(int noteId) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        int delCount = db.delete("note", "id = " + noteId, null);
//        Log.d(TAG, "note deleted rows count = " + delCount);
//
//        dbHelper.close();
//    }

//    public Note getNote(int noteId) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        String sql = "SELECT * FROM note where id = " + noteId;
//        Cursor noteCursor = db.rawQuery(sql,null);
//
//        Note note = getCursorNotes(noteCursor).get(0);
//
//        noteCursor.close();
//        dbHelper.close();
//
//        return note;
//    }

    private List<Training> getCursorTrainings(Cursor trainingCursor) {
        List<Training> trainings = new ArrayList<>();

        if (trainingCursor.moveToFirst()) {
            int idColIndex = trainingCursor.getColumnIndex("id");
            int dateColIndex = trainingCursor.getColumnIndex("date");
            int distanceColIndex = trainingCursor.getColumnIndex("distance");
            int durationColIndex = trainingCursor.getColumnIndex("duration");
            int typeColIndex = trainingCursor.getColumnIndex("type");

            do {
                Training training = new Training();
                training.setId(trainingCursor.getInt(idColIndex));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-LL-dd");
                    training.setDate(LocalDate.parse(trainingCursor.getString(dateColIndex) , formatter));
                }
                training.setDistance(trainingCursor.getDouble(distanceColIndex));
                training.setDuration(trainingCursor.getInt(durationColIndex));
                training.setType(trainingCursor.getInt(typeColIndex));

                trainings.add(training);
            } while (trainingCursor.moveToNext());
        } else Log.d(TAG, "trainings: 0 rows");

        return trainings;
    }

    public List<Training> getTrainings() {
        Log.d(TAG, "start getTrainings");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor trainingCursor = db.query(TRAINING_TABLE, null, null, null, null, null, null);
        List<Training> trainings = getCursorTrainings(trainingCursor);

        trainingCursor.close();
        dbHelper.close();

        return trainings;
    }

//    public List<Note> getNotesByYear(String year) {
//        Log.d(TAG, "" + year);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        //db.rawQuery("SELECT * FROM tblMoment WHERE strftime('%Y', searchDate) = ?", new String[]{year});
//
//        //String sql = "SELECT * FROM note WHERE strftime('%Y', date) = " + year;
//        //Log.d(TAG, sql);
//        Cursor noteCursor = db.rawQuery("SELECT * FROM note WHERE strftime('%Y', date) = ?", new String[]{year});
//
//        List<Note> notes = getCursorNotes(noteCursor);
//        for(Note note : notes) {
//            Log.d(TAG, note.toString());
//        }
//
//        noteCursor.close();
//
//        dbHelper.close();
//
//        return notes;
//    }

}
