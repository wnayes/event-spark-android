package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.ActionBar;
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
	private Event event;
	private LatLng userLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);

		// Receive Event information to display via Intent.
		Intent intent = getIntent();
		this.event = intent.getParcelableExtra("event");
		this.userLocation = intent.getParcelableExtra("userLocation");

		// The home button takes the user back to the map display.
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		// Display the Event title and description.
	    ((TextView)findViewById(R.id.event_details_title)).setText(this.event.getTitle());
	    TextView descBox = (TextView)findViewById(R.id.event_details_description);
	    if (this.event.getDescription().length() == 0) {
	    	descBox.setText(R.string.event_description_empty);
	    	descBox.setTypeface(null, Typeface.ITALIC);
	    }
	    else {
	    	descBox.setText(this.event.getDescription());
	    	descBox.setTypeface(null, Typeface.NORMAL);
	    }
	    
	    // Inform how many users have attended the event using our app.
	    String attending = getResources().getQuantityString(R.plurals.users_attending, this.event.getAttendance(), this.event.getAttendance());
	    ((TextView)findViewById(R.id.event_details_attendance)).setText(attending);
	    
	    // Display different date strings based on the time of the event.
	    Calendar today = Calendar.getInstance();
	    Calendar startCalendar = Calendar.getInstance();
	    startCalendar.setTime(this.event.getStartDate());
	    Calendar endCalendar = Calendar.getInstance();
	    endCalendar.setTime(this.event.getEndDate());

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
            	String endCoords = ((Double)this.event.getLocation().latitude).toString() + "," + ((Double)this.event.getLocation().longitude).toString();
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
