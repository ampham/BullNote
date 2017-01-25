package com.bullnote.buffalo.burgers.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper class.
 */

public class NoteDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Notes.db";

    // Creates the database
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + NoteContract.NoteEntry.TABLE_NAME + "("
            + NoteContract.NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NoteContract.NoteEntry.COLUMN_TITLE + " TEXT NOT NULL, "
            + NoteContract.NoteEntry.COLUMN_BODY + " TEXT NOT NULL);";

    // Clears the database
    public static final String SQL_DELETE_ENTRIES = "DELETE FROM " + NoteContract.NoteEntry.TABLE_NAME;

    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Sets up the database using execSQL, which executes the SQL statement defined above
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}