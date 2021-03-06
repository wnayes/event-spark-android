package com.appchallenge.eventspark;

import java.util.Date;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class MyEvents extends ActionBarActivity implements MyEventsListFragment.ActivityInterface {
	/**
	 * The selected event from the list.
	 */
	Event selectedEvent;
	
	/** Event object for storing the Event to be deleted (persists after selectedEvent is emptied) */
	Event deletionEvent;

	/**
	 * Whether or not the context menu is open.
	 */
	boolean contextMenuOpen = false;

	/** Keeps track of the help view state, used to keep it open / closed as necessary. */
    private Boolean helpOpen;

	/**
     * Provides access to our local sqlite database.
     */
	LocalDatabase localDB;
	
	static final int REQUEST_CODE_MY_EVENTS = 102;

	/** Indicates whether and events have been modified. */
	private Boolean eventsUpdated = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);

        // The home button takes the user back to the map display.
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        // Restore bundle contents.
        if (savedInstanceState != null) {
        	this.contextMenuOpen = savedInstanceState.getBoolean("contextMenuOpen");
        	this.eventsUpdated = savedInstanceState.getBoolean("eventsUpdated");
        	this.selectedEvent = savedInstanceState.getParcelable("selectedEvent");
        	this.deletionEvent = savedInstanceState.getParcelable("deletionEvent");
        }

        // Restore the help dialog if the user has not yet acknowledged it.
        SharedPreferences helpPrefs = getSharedPreferences(Settings.HELP_FILE, 0);
        this.helpOpen = savedInstanceState != null ? savedInstanceState.getBoolean("helpOpen", false) : false;
        if (!helpPrefs.getBoolean(Settings.HELP_MYEVENTS_SEEN, false) || this.helpOpen) {
        	this.helpOpen = true;
        	findViewById(R.id.help_myevents).setVisibility(View.VISIBLE);
        }
        else
        	findViewById(R.id.help_myevents).setVisibility(View.GONE);

        // Persist the context menu state. This required a Runnable to overcome issues
        // with the Activity window not being ready during calls to openContextMenu().
        findViewById(android.R.id.list).post(new Runnable() {
            public void run() {
        		if (contextMenuOpen)
        			openContextMenu(findViewById(android.R.id.list));
            }
        });
    }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("contextMenuOpen", this.contextMenuOpen);
		savedInstanceState.putParcelable("selectedEvent", this.selectedEvent);
		savedInstanceState.putParcelable("deletionEvent", this.deletionEvent);

		// Save the state of the help view.
    	savedInstanceState.putBoolean("helpOpen", this.helpOpen);

    	// Ensure we remember we have updated some events.
    	savedInstanceState.putBoolean("eventsUpdated", this.eventsUpdated);

		super.onSaveInstanceState(savedInstanceState);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_myevents, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
                // Ensure that a proper result is passed to the waiting activity.
        		// If we have modified any events, the listener should be notified.
        		Intent intent = new Intent(MyEvents.this, EventViewer.class);
        		setResult(this.eventsUpdated ? RESULT_OK : RESULT_CANCELED, intent);

        		// Handle going back to the EventViewer without recreating it.
                if (NavUtils.shouldUpRecreateTask(this, intent)) {
                    NavUtils.navigateUpTo(this, intent);
                    finish();
                } else
                    finish();
                return true;
	        case R.id.menu_help:
	        	// Show the help information view overlay.
				this.helpOpen = true;
	            findViewById(R.id.help_myevents).setVisibility(View.VISIBLE);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.d("MyEvents.onCreateContextMenu", "Context menu created.");
        super.onCreateContextMenu(menu, v, menuInfo);
        
        // Retrieve the Event that was selected.
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (this.selectedEvent == null) {
            this.selectedEvent = (Event)(((MyEventsListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_myevents_list))
            		                                                                       .getListAdapter()
            		                                                                       .getItem(info.position));
        }

        // Determine the available actions based on event list section.
        if (this.selectedEvent.getEndDate().before(new Date()))
        	getMenuInflater().inflate(R.menu.my_events_past_context, menu);
        else
            getMenuInflater().inflate(R.menu.my_events_active_context, menu);

        this.contextMenuOpen = true;
    }

	public void onContextMenuClosed(Menu menu) {
		Log.d("MyEvents.onContextMenuClosed", "Context menu closed.");
	    super.onContextMenuClosed(menu);
	    this.contextMenuOpen = false;
	    this.selectedEvent = null;
	}
	
	public boolean onContextItemSelected(android.view.MenuItem item) {
		// Ensure we have selected an item.
		if (selectedEvent == null) {
			Log.e("MyEvents.onContextItemSelected", "No selected event item available.");
			return super.onContextItemSelected((android.view.MenuItem) item);
		}

		if (item.getItemId() == R.id.my_events_details) {
			Intent eventDetails = new Intent(MyEvents.this, EventDetails.class);
	     	eventDetails.putExtra("event", selectedEvent);
	     	startActivity(eventDetails);
		}
		else if (item.getItemId() == R.id.my_events_delete) {
			if (!APICalls.isOnline(this)) {
        		APICalls.displayConnectivityMessage(this);
        		return true;
        	}

	    	this.deletionEvent = selectedEvent;
	    	new deleteEventAPICaller().execute(selectedEvent);
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_update) {
	    	// Pass the event to the Edit Event activity.
	    	Intent editEvent = new Intent(MyEvents.this, EditEvent.class);
	    	editEvent.putExtra("event", selectedEvent);
	    	startActivityForResult(editEvent, EditEvent.REQUEST_CODE_EDIT_EVENT);
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_repost) {
	    	// Allow the user to repost the event by spawning the create event wizard
	    	// with the old event's information pre-filled.
	    	Intent createEvent = new Intent(MyEvents.this, CreateEvent.class);
			createEvent.putExtra("event", selectedEvent);
			startActivityForResult(createEvent, CreateEvent.REQUEST_CODE_CREATE_EVENT);
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_forget) {
	    	// Remove the past event from the cache and refresh the list.
	    	if (localDB == null)
	            localDB = new LocalDatabase(this);
			boolean deleted = localDB.deleteEventFromCache(selectedEvent);
			if (!deleted)
				Log.e("deleteEventAPICaller.onPostExecute", "Could not delete event from local cache");
			else
			    Toast.makeText(this, "Event removed from list.", Toast.LENGTH_SHORT).show();

			((MyEventsListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_myevents_list)).refreshList();
	    	return true;
	    }
	    return super.onContextItemSelected((android.view.MenuItem) item);
	}
	
	/** React to an event being selected from the event list fragment. */
	public void onEventSelected(Event event) {
        // Pass information about the event to the details activity.
        Intent eventDetails = new Intent(MyEvents.this, EventDetails.class);
     	eventDetails.putExtra("event", event);
     	startActivityForResult(eventDetails, EventDetails.REQUEST_CODE_EVENT_DETAILS);
	}
	
	/** Gives Fragments access to the activities database reference. */
	public LocalDatabase getDatabase() {
		if (localDB == null)
            localDB = new LocalDatabase(this);
		return localDB;
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
	}

	public void onBackPressed() {
		// Ensure that a proper result is passed to the waiting activity.
		// If we have modified any events, the listener should be notified.
		Intent intent = new Intent(MyEvents.this, EventViewer.class);
		setResult(this.eventsUpdated ? RESULT_OK : RESULT_CANCELED, intent);
	    super.onBackPressed();
	}

	/**
     * Receives the result of the event creation wizard.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d("MyEvents.onActivityResult", "Received result intent. resultCode: " + resultCode);

    	// Received a result indicating some event(s) have been updated.
    	if ((requestCode == CreateEvent.REQUEST_CODE_CREATE_EVENT && resultCode == RESULT_OK) ||
    		(requestCode == EditEvent.REQUEST_CODE_EDIT_EVENT && resultCode == RESULT_OK) ||
    		(requestCode == EventDetails.REQUEST_CODE_EVENT_DETAILS && resultCode == RESULT_OK)) {
    		((MyEventsListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_myevents_list)).refreshList();
    		this.eventsUpdated = true;
    	}
    }

	/** Closes the help information view. */
    public void onCloseHelpClick(View v) {
    	assert this.helpOpen;

    	// Ensure that we remember we have already seen this help.
    	SharedPreferences helpPrefs = getSharedPreferences(Settings.HELP_FILE, 0);
        helpPrefs.edit().putBoolean(Settings.HELP_MYEVENTS_SEEN, true).commit();

        // Hide the help view.
        findViewById(R.id.help_myevents).setVisibility(View.GONE);
        this.helpOpen = false;
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
			// Close any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == false) {
				Toast.makeText(getApplicationContext(), "The event could not be deleted!", Toast.LENGTH_LONG).show();
				return;
			}

			// Update the local cache to recognize deletion.
			if (localDB == null)
	            localDB = new LocalDatabase(getApplicationContext());
			boolean deleted = localDB.deleteEventFromCache(deletionEvent);
			if (!deleted)
				Log.e("deleteEventAPICaller.onPostExecute", "Could not delete event from local cache");
			else
				Toast.makeText(getApplicationContext(), "Deleted Event.", Toast.LENGTH_LONG).show();

			// Update the list view and internal events list.
			deletionEvent = null;
			((MyEventsListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_myevents_list)).refreshList();
			eventsUpdated = true;
		}
	}
}
