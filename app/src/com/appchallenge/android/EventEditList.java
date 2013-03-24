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

public class EventEditList extends SherlockListActivity {

	private ArrayList<Event> currentEvents = new ArrayList<Event>();
	private ArrayList<String> eventTitle = new ArrayList<String>();
	private ArrayList<Event> ownedEvents = new ArrayList<Event>();
	LocalDatabase localDB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        
        // The home button takes the user back to the map display.
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.event_edit_list_layout);
        
		String ownerId = "";
		        
        Intent intent = getIntent();
        currentEvents = intent.getParcelableArrayListExtra("currentEvents");
        
		if (localDB == null)
			localDB = new LocalDatabase(getApplicationContext());
		
		//Populates the list with all the events you own
		for (final Event event : currentEvents) {
			
			ownerId = localDB.getEventSecretId(event);
						
			if (ownerId != null && ownerId.length() > 0) {
				Log.d("EventEditList", "Found Owned Event");
				if (event.getTitle().length() > 40)
					eventTitle.add(event.getTitle().substring(0,40));
				else
					eventTitle.add(event.getTitle());
					ownedEvents.add(event);
				}
			}
		
		ArrayAdapter<String> events = new ArrayAdapter<String>(this, 
								android.R.layout.simple_list_item_1, eventTitle);
	    setListAdapter(events);
		
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
	      super.onListItemClick(l, v, position, id);
	      if (ownedEvents == null) {
	    	  Toast.makeText(this, "Something Went Wrong Please Try Again.", Toast.LENGTH_LONG).show();
	    	  this.finish();
	    	  return;
	      }
	      Event event = ownedEvents.get(position);
	      Intent eventEdit = new Intent(EventEditList.this, EventEdit.class);
	      eventEdit.putExtra("event", event);
	      startActivity(eventEdit);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home){
			NavUtils.navigateUpTo(this, new Intent(this, EventViewer.class));
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
		eventTitle.clear();
	}

}
