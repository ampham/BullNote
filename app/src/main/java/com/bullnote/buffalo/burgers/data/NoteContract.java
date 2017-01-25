package com.bullnote.buffalo.burgers.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for notes. There is only one table; it contains the notes.
 */
public class NoteContract {

    // Does not need a constructor, should never be instantiated
    private NoteContract() {}

    // String and URI constants for the ContentProvider
    public static final String CONTENT_AUTHORITY = "com.example.android.notepad";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NOTES = "notes";

    /**
     * Inner class that defines constant values for the notes database table.
     * Each entry in the table represents a single note with a title and a body.
     */
    public static class NoteEntry implements BaseColumns {

        // The content URI to access the pet data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTES);

        // The MIME type of the CONTENT_URI for a list of pets.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

        // The MIME type of the CONTENT_URI for a single pet.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

        // Gets _ID from BaseColumns
        public static final String _ID = BaseColumns._ID;

        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BODY = "body";
    }
}

