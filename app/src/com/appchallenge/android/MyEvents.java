package com.appchallenge.android;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
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
     * Provides access to our local sqlite database.
     */
	LocalDatabase localDB;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);

        // The home button takes the user back to the map display.
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

		if (localDB == null)
			localDB = new LocalDatabase(getApplicationContext());

		// Get a list of the events we have created over time.
		myEvents = localDB.getMyEvents();

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
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
	      super.onListItemClick(l, v, position, id);
	      if (myEvents == null) {
	    	  Log.e("MyEvents.onListItemClick", "ArrayList<Event> myEvents is null.");
	    	  return;
	      }

	      Event event = myEvents.get(position);
	      Intent eventEdit = new Intent(MyEvents.this, EditEvent.class);
	      eventEdit.putExtra("event", event);
	      startActivity(eventEdit);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			// Handle going back to the EventViewer without recreating it.
			Intent viewerIntent = new Intent(this, EventViewer.class);
            if (NavUtils.shouldUpRecreateTask(this, viewerIntent)) {
                NavUtils.navigateUpTo(this, viewerIntent);
                finish();
            } else
                finish();
            return true;
		}
		super.onOptionsItemSelected(item);
		return true;
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
		myEvents.clear();
	}
}
