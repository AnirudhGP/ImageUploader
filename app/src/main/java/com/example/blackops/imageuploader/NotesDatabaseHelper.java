package com.example.blackops.imageuploader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by aniru on 13-10-2016.
 */

public class NotesDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "notesManager";
    public static final String TABLE_PENDING_NOTES = "pendingNotes";
    public static final String KEY_COURSEID = "courseId";
    public static final String KEY_URILIST = "uriList";
    public static final String KEY_NOOFPAGESUPLOADED = "noOfPagesUploaded";


    public NotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PENDING_NOTES + "("
                + KEY_COURSEID + " TEXT PRIMARY KEY," + KEY_URILIST + " TEXT" + KEY_NOOFPAGESUPLOADED +
                " INTEGER, " + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_NOTES);

        // Create tables again
        onCreate(db);
    }

    public void insertPendingNote(PendingNote notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_COURSEID, notes.getCourseId());
        values.put(KEY_URILIST, notes.getUriList());
        values.put(KEY_NOOFPAGESUPLOADED, notes.getNoOfPagesUploaded());

        db.insert(TABLE_PENDING_NOTES, null, values);
        db.close();
    }

    public PendingNote getPendingNote(String courseId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PENDING_NOTES, new String[] { KEY_COURSEID,
                        KEY_URILIST, KEY_NOOFPAGESUPLOADED }, KEY_COURSEID + "=?",
                new String[] { String.valueOf(courseId) }, null, null,
                null, null);
        if (cursor != null)
            cursor.moveToFirst();
        else {
            return new PendingNote(null, null, 0);
        }

        PendingNote pendingNote = new PendingNote(cursor.getString(0),
                cursor.getString(1), Integer.parseInt(cursor.getString(3)));

        return pendingNote;
    }

    public PendingNote getNextPendingNote() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PENDING_NOTES, new String[] { KEY_COURSEID,
                KEY_URILIST, KEY_NOOFPAGESUPLOADED }, null, null, null, null, null,
                null);

        if (cursor != null)
            cursor.moveToFirst();
        else {
            return new PendingNote(null, null, 0);
        }
        PendingNote pendingNote = new PendingNote(cursor.getString(0),
                cursor.getString(1), Integer.parseInt(cursor.getString(3)));

        return pendingNote;
    }

    public int getPendingNotesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PENDING_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    public int incrementPageNumberOfPendingNote(String courseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        PendingNote note = getPendingNote(courseId);
        ContentValues values = new ContentValues();

        values.put(KEY_NOOFPAGESUPLOADED, note.getNoOfPagesUploaded() + 1);

        return db.update(TABLE_PENDING_NOTES, values, KEY_COURSEID + "=?",
                new String[] {note.getCourseId()});
    }

    public void deletePendingNote(String courseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING_NOTES, KEY_COURSEID + " = " + courseId, null);
        db.close();
    }
}