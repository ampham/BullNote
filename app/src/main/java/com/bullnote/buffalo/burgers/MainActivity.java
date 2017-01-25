package com.bullnote.buffalo.burgers;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bullnote.buffalo.burgers.data.NoteContract;

/**
 * The main activity of this app. It lists all of the notes in a ListView and has a button the user
 * can click to add a note.
 *
 * Notes are loaded from the database on a background thread using a Loader and the ListView uses a
 * CursorAdapter to recycle views.
 * */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // An integer constant for the Loader
    private static final int NOTE_LOADER = 0;

    // Make the CursorAdapter an instance variable because it's used a lot
    NoteCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The view to click to add a new note - should probably not be a text view
        TextView newNote = (TextView) findViewById(R.id.new_note);
        newNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView to populate with notes and attach an adapter to it
        // Since there is no data yet (that comes with the loader) we pass in null
        ListView notesListView = (ListView) findViewById(R.id.list);

        mCursorAdapter = new NoteCursorAdapter(this, null);
        notesListView.setAdapter(mCursorAdapter);

        // Set click listeners on each item that lead to their Edit page
        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create a new intent to go to the Editor page
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);

                // Form the Content URI for the note that was clicked on by appending the id
                // (remember we used it back in onCreateLoader) onto the CONTENT_URI
                Uri currentNoteUri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, id);

                // Set the URI onto the intent so the fields of the Edit page can be populated
                intent.setData(currentNoteUri);

                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(NOTE_LOADER, null, this);
    }

    /**
     * Implementing LoaderCallbacks interface methods:
     *
     * onCreateLoader defines the Cursor that will be returned from the database: what columns we
     * want, etc.
     *
     * onLoadFinished actually passes the data off to the adapter
     *
     * onLoaderReset clears the cursor to prevent memory leaks
     * */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we want
        // We must specify _ID because the CursorAdapter expects the Cursor to have an _ID column
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_TITLE,
                NoteContract.NoteEntry.COLUMN_BODY
        };

        // the Loader will now execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                   // Parent activity context
                NoteContract.NoteEntry.CONTENT_URI,  // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null                    // Default sort order
        );
    }

    // Note that the Loader<Cursor> object being passed in here is the one that was returned by
    // onCreateLoader. This method updates the adapter with the data returned from the database
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}

