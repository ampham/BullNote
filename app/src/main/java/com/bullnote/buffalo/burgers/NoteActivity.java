package com.bullnote.buffalo.burgers;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bullnote.buffalo.burgers.data.NoteContract;


/**
 * NoteActivity is the editor page of the app. In the onCreate method we grab the intent
 * that took the user here; if its data is null then it's in "New Note" mode, else it's in
 * "Edit Note" mode and initializes the Loader to fill in the relevant title and body.
 * */
public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the note data loader
    private static final int EXISTING_NOTE_LOADER = 0;

    // Content URI for the existing note (null if it's a new note)
    private Uri mCurrentNoteUri;

    // Note title and body views + buttons
    private EditText mTitleBox;
    private EditText mBodyBox;
    private Button mSaveButton;
    private Button mDeleteButton;

    // Boolean flag to track if the fields have been edited or not; false (not touched) by default
    private boolean mNoteHasChanged = false;

    // OnTouchListener that listens for any touches on a view. If a view is touched, it flags
    // mNoteHasChanged to notify the user if they try to navigate away after touching anything
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mNoteHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Get a reference to the views and the save button
        mTitleBox = (EditText) findViewById(R.id.newNoteTitle);
        mBodyBox = (EditText) findViewById(R.id.newNoteBody);
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mDeleteButton = (Button) findViewById(R.id.deleteButton);

        // Set the touch listener on the views
        mTitleBox.setOnTouchListener(mTouchListener);
        mBodyBox.setOnTouchListener(mTouchListener);

        // Save the note when the user clicks the "Save" button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        // Get the intent that started this activity and its data (URI)
        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();

        // If the intent is null, we're in New Note mode. Set the title and the correct behavior
        // for the "Delete" button
        if (mCurrentNoteUri == null){
            setTitle("New Note");

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showExitConfirmationDialog();
                }
            });
        } else {
            // Otherwise this is an existing note, so change the title to "Edit Note" and get the
            // note data from the loader, and set the Delete button to actually delete if pressed
            setTitle("Edit Note");
            getLoaderManager().initLoader(EXISTING_NOTE_LOADER, null, this);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });
        }
    }

    // Saves the note to the database and finishes the Activity
    private void saveNote(){
        // Grab the text from the fields
        String noteTitle = mTitleBox.getText().toString().trim();
        String noteBody = mBodyBox.getText().toString().trim();

        // Do nothing if this is a new note and all the fields are blank
        if (mCurrentNoteUri == null && TextUtils.isEmpty(noteTitle) && TextUtils.isEmpty(noteBody)){
            return;
        }

        // Get a ContentValues object; it will be needed whether it's a new note or an update
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_TITLE, noteTitle);
        values.put(NoteContract.NoteEntry.COLUMN_BODY, noteBody);

        // If the Content URI is null, it's a new pet, so we add one to the database
        if (mCurrentNoteUri == null){
            Uri newUri = getContentResolver().insert(NoteContract.NoteEntry.CONTENT_URI, values);

            // Log some toast to the screen to confirm success or indicate failure
            if (newUri == null) {
                Toast.makeText(this, "Save failed, please try again.", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, "Note has been saved", Toast.LENGTH_LONG);
            }
        } else {
            // Otherwise this is an existing note, so update it with the Content URI; Pass in null
            // for the selection and selection args because mCurrentPetUri will already identify
            // the correct row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentNoteUri, values, null, null);

            // Log some toast to the screen to confirm success or indicate failure
            if (rowsAffected == 0) {
                Toast.makeText(this, "Save failed, please try again.", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, "Note has been updated", Toast.LENGTH_LONG);
            }
        }

        // Exit back to the main screen because we're done here
        finish();
    }

    //Confirm that the user wants to leave the activity when pressing "Delete" on a new note
    private void showExitConfirmationDialog(){
        // Create an AlertDialog and set the confirmation click to delete the note
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This note will not be saved. Are you sure?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the above dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Prompt the user to confirm that they want to delete this note
    private void showDeleteConfirmationDialog(){
        // Create an AlertDialog and set the confirmation click to delete the note
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This note will be deleted permanently. Are you sure?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteNote();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the above dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Delete a note from the database
    private void deleteNote(){
        // Only perform the delete if this is an existing note
        if (mCurrentNoteUri != null){
            // Call the ContentResolver to delete the note at the given URI
            // Pass in null for selection and selectionArgs because the URI is the note we want
            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);

            // Log some toast to the screen to confirm success or indicate failure
            if (rowsDeleted == 0){
                Toast.makeText(this, "Error: Could not delete.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /** Shows an alert dialog when the user has unsaved changes and presses Up or Back */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonListener){
        // Create an AlertDialog.Builder and set the message and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Discard unsaved changes?");
        builder.setPositiveButton("Discard", discardButtonListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog and carry on
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Override the Back button's normal behavior to notify the user of unsaved changes, if any
    @Override
    public void onBackPressed() {
        // If the note hasn't been touched, proceed normally
        if (!mNoteHasChanged){
            super.onBackPressed();
            return;
        }

        // Otherwise there are unsaved changes, so we set up a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked the "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show the dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonListener);
    }

    /** Implementing LoaderCallbacks interface methods */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Get a projection with the columns we want; remember the CursorAdapter needs its _ID
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_TITLE,
                NoteContract.NoteEntry.COLUMN_BODY
        };

        // Execute the ContentProvider's query method
        return new CursorLoader(
                this,               // Parent activity's context
                mCurrentNoteUri,    // Query the content URI for the current pet
                projection,         // Columns to include in the resulting Cursor
                null,               // No selection clause
                null,               // No selection arguments
                null                // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Nothing to do if the cursor is null or there is less than one row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Read the data from the first row of the cursor (there should be only one)
        if (cursor.moveToFirst()){
            // Get the column numbers of the data we want
            int titleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TITLE);
            int bodyColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_BODY);

            // Extract the data from the columns
            String title = cursor.getString(titleColumnIndex);
            String body = cursor.getString(bodyColumnIndex);

            // Update the views with the title and body
            mTitleBox.setText(title);
            mBodyBox.setText(body);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleBox.setText("");
        mBodyBox.setText("");
    }
}

