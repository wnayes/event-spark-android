package com.appchallenge.android;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyEvents extends SherlockListActivity {
	/**
	 * List of events that the user has made over time.
	 */
	private ArrayList<Event> myEvents = new ArrayList<Event>();

	/**
	 * The position of the event we selected from the list.
	 */
	int selectedIndex = -1;

	/**
	 * Whether or not the context menu is open.
	 */
	boolean contextMenuOpen = false;

	/**
     * Provides access to our local sqlite database.
     */
	LocalDatabase localDB;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);
		registerForContextMenu(getListView());

        // The home button takes the user back to the map display.
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        // Restore bundle contents.
        if (savedInstanceState != null) {
        	this.selectedIndex = savedInstanceState.getInt("selectedIndex");
        	this.contextMenuOpen = savedInstanceState.getBoolean("contextMenuOpen");
        }

        if (localDB == null)
            localDB = new LocalDatabase(getApplicationContext());

        // Get a list of the events we have created over time.
        this.myEvents = localDB.getMyEvents();

		// Extract the titles of these events.
		ArrayList<String> titles = new ArrayList<String>();
		for (Event e : myEvents) {
			if (e.getTitle().length() > 40)
			    titles.add(e.getTitle().substring(0, 40));
			else
				titles.add(e.getTitle());
		}

        if (myEvents.size() > 0) {
		    ArrayAdapter<String> events
              = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
	        setListAdapter(events);
        }
        else
        	findViewById(R.id.my_events_empty).setVisibility(View.VISIBLE);

        // Persist the context menu state. This required a Runnable to overcome issues
        // with the Activity window not being ready during calls to openContextMenu().
        findViewById(android.R.id.list).post(new Runnable() {
            public void run() {
                // Reopen the context menu on configuration changes.
                if (contextMenuOpen)
                    openContextMenu(getListView());
            }
        });
    }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("selectedIndex", this.selectedIndex);
		savedInstanceState.putBoolean("contextMenuOpen", this.contextMenuOpen);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.my_events_menu_layout, menu);
        this.contextMenuOpen = true;
    }

	public void onContextMenuClosed(ContextMenu menu) {
	    super.onContextMenuClosed(menu);
	    this.contextMenuOpen = false;
	    this.selectedIndex = -1;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	// Handle going back to the EventViewer without recreating it.
                Intent viewerIntent = new Intent(this, EventViewer.class);
                if (NavUtils.shouldUpRecreateTask(this, viewerIntent)) {
                    NavUtils.navigateUpTo(this, viewerIntent);
                    finish();
                } else
                    finish();
                return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public boolean onContextItemSelected(android.view.MenuItem item) {
		// Ensure we have selected an item.
		if (selectedIndex == -1) {
			Log.e("MyEvents.onContextItemSelected", "No selected list item available.");
			return super.onContextItemSelected((android.view.MenuItem) item);
		}

	    if (item.getItemId() == R.id.my_events_delete) {
	    	deleteEventAPICaller deleteEvent = new deleteEventAPICaller();
			//deleteEvent.execute(selectedEvent);
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_repost) {
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_update) {
	    	// Pass the event to the Edit Event activity.
	    	Intent editEvent = new Intent(MyEvents.this, EditEvent.class);
	    	editEvent.putExtra("event", myEvents.get(selectedIndex));
	    	startActivity(editEvent);
	    	return true;
	    }
	    return super.onContextItemSelected((android.view.MenuItem) item);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (myEvents == null) {
        	Log.e("MyEvents.onListItemClick", "ArrayList<Event> myEvents is null.");
            return;
        }

        this.selectedIndex = position;
        openContextMenu(v);
	}

	@Override
    protected void onPause() {
    	super.onPause();

    	// Close our database helper if necessary.
    	if (localDB != null)
            localDB.close();
    }
	
	protected void onDestroy() {
		super.onDestroy();
		closeContextMenu();
		myEvents.clear();
	}
	
	private class deleteEventAPICaller extends AsyncTask<Event, Void, Boolean> {
		/**
	     * Informs the user that the event is being deleted.
	     */
	    ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// Set up progress indication.
			dialog = ProgressDialog.show(MyEvents.this, "Deleting...", "");
		}

		@Override
		protected Boolean doInBackground(Event... event) {
			return APICalls.deleteEvent(event[0], Identity.getUserId(getApplicationContext()));
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Close the wizard and any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == false) {
				(Toast.makeText(getApplicationContext(), "Event already deleted, or could not delete event!", Toast.LENGTH_LONG)).show();
				MyEvents.this.finish();
				return;
			}
			
			// Pass the new event to the event viewer.
			//Intent intent = new Intent(EventEdit.this, EventViewer.class);
			//intent.putExtra("event", result);
		    //EventEdit.this.setResult(RESULT_OK, intent);
			MyEvents.this.finish();
		}
	}

}
