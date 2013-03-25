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

	private ArrayList<Event> currentEvents = new ArrayList<Event>();
	private ArrayList<String> eventTitles = new ArrayList<String>();
	private ArrayList<Event> ownedEvents = new ArrayList<Event>();
	LocalDatabase localDB;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);

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
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
	      super.onListItemClick(l, v, position, id);
	      if (ownedEvents == null) {
	    	  Toast.makeText(this, "Something Went Wrong Please Try Again.", Toast.LENGTH_LONG).show();
	    	  this.finish();
	    	  return;
	      }
	      Event event = ownedEvents.get(position);
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
	
	protected void onStop() {
		super.onStop();
		
	}
	
	protected void onDestroy() {
		super.onDestroy();
		ownedEvents.clear();
		eventTitles.clear();
	}
}
