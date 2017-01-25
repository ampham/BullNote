package com.bullnote.buffalo.burgers;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bullnote.buffalo.burgers.data.NoteContract;

/**
 * CursorAdapter implementation for loading notes into the main activity's ListView.
 */

public class NoteCursorAdapter extends CursorAdapter {
    public NoteCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    // Makes a new blank list view item; no data is bound to the view yet
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // Binds the note data to the view it will be populating
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Get a reference to the views we'll be using
        TextView titleView = (TextView) view.findViewById(R.id.list_item_title);

        // Find the columns in the cursor that contain the data we want
        int titleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TITLE);

        // Pull the title and body Strings out of the cursor
        String noteTitle = cursor.getString(titleColumnIndex);

        // If the title is empty, put (No title)
        if (noteTitle.length() == 0){
            noteTitle = "(No title)";
        }

        // Set the text to the views
        titleView.setText(noteTitle);
    }
}
