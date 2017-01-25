package com.bullnote.buffalo.burgers.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Content Provider for interacting with the database
 */

public class NoteProvider extends ContentProvider {

    // URI matcher codes for the content URI of the pets table and a single pet
    public static final int NOTES = 100;
    public static final int NOTE_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /*
    * The calls to addURI() go here, for all of the content URI patterns that the provider
    * should recognize. All paths added to the UriMatcher have a corresponding code to return
    * when a match is found.
    * */
    static {
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTES);
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES + "/#", NOTE_ID);
    }

    // Log tag
    public static final String LOG_TAG = NoteProvider.class.getSimpleName();

    // Database helper
    private NoteDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Initialize the database helper as soon as the Activity is created
        mDbHelper = new NoteDbHelper(getContext());

        return true;
    }

    // Perform a query for the given URI
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get a readable database to query and the cursor that will be returned
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        // get the URI being passed in and hit the database accordingly
        int match = sUriMatcher.match(uri);
        switch(match){
            case NOTES:
                // return the the entire table
                cursor = database.query(
                        NoteContract.NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case NOTE_ID:
                // For the NOTE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/notes/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(
                        NoteContract.NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set a notification URI on the Cursor so if the underlying data changes, then the UI can
        // be updated too; the actual updating happens in the notifyChange method, which is
        // called in the other methods in this class
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /** Inserts a new row into the database; calls the helper method below */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case NOTES:
                return insertNote(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertNote(Uri uri, ContentValues contentValues){
        // Sanity checks go here
        String title = contentValues.getAsString(NoteContract.NoteEntry.COLUMN_TITLE);
        String body = contentValues.getAsString(NoteContract.NoteEntry.COLUMN_BODY);
        if (title == null || body == null){
            throw new IllegalArgumentException("Fields cannot be empty!");
        }

        // get a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert a new note into the database, returning the ID of that new row.
        // The first argument for db.insert() is the database table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for the note.
        long id = database.insert(NoteContract.NoteEntry.TABLE_NAME, null, contentValues);
        // If the id is -1, the insertion failed. Log an error and return null
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /** Updates entries in the database */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case NOTES:
                return updateNote(uri, contentValues, selection, selectionArgs);
            case NOTE_ID:
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateNote(uri,contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not possible for " + uri);
        }
    }

    private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // Sanity checks for title and body
        if (values.containsKey(NoteContract.NoteEntry.COLUMN_TITLE)){
            String title = values.getAsString(NoteContract.NoteEntry.COLUMN_TITLE);
            if (title == null){
                throw new IllegalArgumentException("Needs a title!");
            }
        }

        if (values.containsKey(NoteContract.NoteEntry.COLUMN_BODY)){
            String body = values.getAsString(NoteContract.NoteEntry.COLUMN_BODY);
            if (body == null){
                throw new IllegalArgumentException("Note cannot be empty!");
            }
        }

        // If nothing's there, return early because there's nothing to update
        if (values.size() == 0){
            return 0;
        }

        // If the values are note empty, get a writable database and update it with the values
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(NoteContract.NoteEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            // Delete all rows that match the criteria
            case NOTES:
                rowsDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // Delete a single row that matches the criteria
            case NOTE_ID:
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case NOTES:
                return NoteContract.NoteEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteContract.NoteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}