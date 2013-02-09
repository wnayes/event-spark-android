package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EventDetails extends SherlockFragmentActivity {
	// Private members containing the Event information.
	private int id;
	private String title;
	private String description;
	private Date startDate;
	private Date endDate;
	private int attendance;
	private LatLng location;
	private LatLng userLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);

		// Receive Event information to display via Intent.
		Intent intent = getIntent();
		this.id = intent.getIntExtra("id", -1);
		this.title = intent.getStringExtra("title");
		this.description = intent.getStringExtra("description");
		this.startDate = (Date)intent.getSerializableExtra("startDate");
		this.endDate = (Date)intent.getSerializableExtra("endDate");
		this.location = new LatLng(intent.getDoubleExtra("latitude", 0),
		                           intent.getDoubleExtra("longitude", 0));
		this.userLocation = new LatLng(intent.getDoubleExtra("userLatitude", 0),
				                       intent.getDoubleExtra("userLongitude", 0));
		this.attendance = intent.getIntExtra("attendance", 1);

		// The home button takes the user back to the map display.
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		// Display the Event title and description.
	    ((TextView)findViewById(R.id.event_details_title)).setText(this.title);
	    TextView descBox = (TextView)findViewById(R.id.event_details_description);
	    if (this.description.isEmpty()) {
	    	descBox.setText(R.string.event_description_empty);
	    	descBox.setTypeface(null, Typeface.ITALIC);
	    }
	    else {
	    	descBox.setText(this.description);
	    	descBox.setTypeface(null, Typeface.NORMAL);
	    }
	    
	    // Display different date strings based on the time of the event.
	    Calendar today = Calendar.getInstance();
	    Calendar startCalendar = Calendar.getInstance();
	    startCalendar.setTime(this.startDate);
	    Calendar endCalendar = Calendar.getInstance();
	    endCalendar.setTime(this.endDate);

	    String dateString = "";
	    if (endCalendar.before(today)) {
	    	dateString += "This event has ended.";
	    	((TextView)findViewById(R.id.event_details_date_description)).setText(dateString);
	    }
	    else {
		    if (startCalendar.after(today)) {
		    	if (startCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
		    		dateString += "Begins today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + ". ";
		    	else
		    		dateString += "Begins tomorrow at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + ". ";
		    }
	
		    if (endCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
	    		dateString += "Ends today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + ".";
	    	else
	    		dateString += "Ends tomorrow at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + ".";
	    }
	    
	    ((TextView)findViewById(R.id.event_details_date_description)).setText(dateString);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_event_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	    	    // Return to the EventViewer.
	            finish();
	            return true;
	        case R.id.menu_refresh_event:
	        	
	        	return true;
	        case R.id.menu_get_directions:
	        	// Prepare maps url query url parameters.
            	String startCoords = ((Double)this.userLocation.latitude).toString() + "," + ((Double)this.userLocation.longitude).toString();
            	String endCoords = ((Double)this.location.latitude).toString() + "," + ((Double)this.location.longitude).toString();
            	String url = "http://maps.google.com/maps?saddr=" + startCoords + "&daddr=" + endCoords;
            	Log.d("EventDetailsLocationTab", "Get directions, " + url);

                // Pass an intent to an activity that can provide directions.
            	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
	        default:
	            return super.onOptionsItemSelected(item);
	   }
	}
}
