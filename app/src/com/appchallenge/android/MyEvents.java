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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyEvents extends SherlockListActivity {

	private ArrayList<Event> currentEvents = new ArrayList<Event>();
	private ArrayList<String> eventTitles = new ArrayList<String>();
	private ArrayList<Event> ownedEvents = new ArrayList<Event>();
	int listPosition = 0;
	Event selectedEvent;
	LocalDatabase localDB;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);
		registerForContextMenu(getListView());

        // The home button takes the user back to the map display.
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

		String ownerId = "";

        Intent intent = getIntent();
        currentEvents = intent.getParcelableArrayListExtra("currentEvents");

		if (localDB == null)
			localDB = new LocalDatabase(getApplicationContext());

		// Populates the list with all the events you own
        for (final Event event : currentEvents) {
			
            ownerId = localDB.getEventSecretId(event);
						
            if (ownerId != null && ownerId.length() > 0) {
				Log.d("EventEditList", "Found Owned Event");
				if (event.getTitle().length() > 40)
                    eventTitles.add(event.getTitle().substring(0,40));
				else
                    eventTitles.add(event.getTitle());
                ownedEvents.add(event);
            }
        }

        if (eventTitles.size() > 0) {
		    ArrayAdapter<String> events
              = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventTitles);
	        setListAdapter(events);
        }
        else
        	findViewById(R.id.my_events_empty).setVisibility(View.VISIBLE);
        
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_events_menu_layout, menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Log.d("which", "one");
	    switch (item.getItemId()) {
	        case android.R.id.home:
				NavUtils.navigateUpTo(this, new Intent(this, EventViewer.class));
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	            
	    }
	}
	
	
	public boolean onContextItemSelected(android.view.MenuItem item) {
		
	    if (item.getItemId() == R.id.my_events_delete && selectedEvent != null) {
	    	deleteEventAPICaller deleteEvent = new deleteEventAPICaller();
			//deleteEvent.execute(selectedEvent);
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_repost){
	    	return true;
	    }
	    else if (item.getItemId() == R.id.my_events_update && selectedEvent !=null){
	    	Intent editEvent = new Intent(MyEvents.this, EditEvent.class);
	    	editEvent.putExtra("event", selectedEvent);
	    	startActivity(editEvent);
	    	return true;
	    }
	    return super.onContextItemSelected((android.view.MenuItem) item);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);
	    if (ownedEvents == null) {
	    	  Toast.makeText(this, "Something Went Wrong Please Try Again.", Toast.LENGTH_LONG).show();
	    	  this.finish();
	    	  return;
	      }
	      selectedEvent = ownedEvents.get(position);
	      openContextMenu(v);
	      //startActivity(eventEdit);
	}
		
	@Override
    protected void onPause() {
    	super.onPause();
    	// Close our database helper if necessary.
    	if (localDB != null)
            localDB.close();
    }
	
	protected void onStop() {
		super.onStop();
		
	}
	
	protected void onDestroy() {
		super.onDestroy();
		ownedEvents.clear();
		eventTitles.clear();
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
