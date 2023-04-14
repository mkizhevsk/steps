package com.mk.steps.data.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mk.steps.data.DBHelper;
import com.mk.steps.data.entity.Training;

import java.time.format.DateTimeFormatter;

public class BaseService extends Service {

    private final IBinder mBinder = new LocalBinder();

    DBHelper dbHelper;

    private static final String BASE_NAME = "my_music.db";
    private static final String TRAINING_TABLE = "training";



    final String TAG = "myLogs";

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

//    private List<Note> getCursorNotes(Cursor noteCursor) {
//        List<Note> notes = new ArrayList<>();
//
//        if (noteCursor.moveToFirst()) {
//            int idColIndex = noteCursor.getColumnIndex("id");
//            int dateColIndex = noteCursor.getColumnIndex("date");
//            int contentColIndex = noteCursor.getColumnIndex("content");
//
//            do {
//                Note note = new Note();
//                note.setId(noteCursor.getInt(idColIndex));
//                note.setDate(LocalDate.parse(noteCursor.getString(dateColIndex) , formatter));
//                note.setContent(noteCursor.getString(contentColIndex));
//
//                notes.add(note);
//            } while (noteCursor.moveToNext());
//        } else Log.d(TAG, "notes: 0 rows");
//
//        return notes;
//    }

//    public List<Note> getNotes() {
//        Log.d(TAG, "start getNotes");
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        Cursor noteCursor = db.query("note", null, null, null, null, null, null);
//
//        List<Note> notes = getCursorNotes(noteCursor);
//
//        noteCursor.close();
//
//        dbHelper.close();
//
//        return notes;
//    }

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


    public class LocalBinder extends Binder {
        public BaseService getService() {
            return BaseService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
