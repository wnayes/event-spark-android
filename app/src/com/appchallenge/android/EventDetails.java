package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class EventDetails extends SherlockFragmentActivity {
	// Private members containing the Event information.
	private Event event;
	private LatLng userLocation;

    /**
     * Provides access to our local sqlite database.
     */
    private LocalDatabase localDB;

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

		this.updateEventDetails();
	}

	@Override
    protected void onPause() {
    	super.onPause();

    	// Close our database helper if necessary.
    	if (localDB != null)
            localDB.close();
    }
	

	/**
	 * Updates the UI with the latest copy of the Event we have.
	 */
	private void updateEventDetails() {
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
		    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + " Today — ";
		    	else
		    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + " Tomorrow — ";
		    }
		    else
		    	dateString += "Now — ";
	
		    if (endCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
	    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + " Today";
	    	else
	    		dateString += DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + " Tomorrow";
	    }
	    
	    ((TextView)findViewById(R.id.event_details_date_description)).setText(dateString);
	}

	private Menu _menu;
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_event_details, menu);
		
		// Keep a reference to the menu for later uses (refresh indicator change).
        this._menu = menu;
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
	        	// Grab a new copy of the event.
	        	new updateEventDetailsAPICaller().execute(this.event.getId());
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
	        case R.id.menu_report_event:
	        	
	        	return true;
            case R.id.menu_attend_event:
            	
            	String[] inputList = new String[2];
            	inputList[0] = Integer.toString(this.event.getId());
            	inputList[1] = Identity.getUserId(getApplicationContext());
            	 attendEventAPICaller attend = new attendEventAPICaller();
            	 attend.execute(inputList);
			try {
				int result = attend.get();
				if (result == 0){
					(Toast.makeText(getApplicationContext(), "You Are Already Going!", Toast.LENGTH_LONG)).show();
				} else {
					String attending = getResources().getQuantityString(R.plurals.users_attending, result, result);
				    ((TextView)findViewById(R.id.event_details_attendance)).setText(attending);
					(Toast.makeText(getApplicationContext(), "Joined the Event", Toast.LENGTH_LONG)).show();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            	 
            	
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	   }
	}
	
	private class attendEventAPICaller extends AsyncTask<String[], Integer, Integer> {
		/**
	     * Informs the user that the event is being created.
	     */
	    ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// Set up progress indication.
			dialog = ProgressDialog.show(EventDetails.this, "Finding Attendees...", "");
		}

		@Override
		protected Integer doInBackground(String[]... attend) {
			return APICalls.joinAttendance(attend[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			dialog.dismiss();
			dialog = null;
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Couldn't load attending!", Toast.LENGTH_LONG)).show();
				return;
			}
		}
	}
	
	/**
	 * Performs an asynchronous API call receive any updates of the event we are viewing.
	 */
	private class updateEventDetailsAPICaller extends AsyncTask<Integer, Void, Event> {
		/**
		 * Quick access to the refresh button in the actionbar.
		 */
		MenuItem refreshItem;

		protected void onPreExecute() {
			// Establish progress UI changes.
			if (_menu != null) {
		        refreshItem = _menu.findItem(R.id.menu_refresh_event);
		        if (refreshItem != null)
			        refreshItem.setActionView(R.layout.actionbar_refresh_progress);
			}
		}

		protected Event doInBackground(Integer... id) {
			return APICalls.getEvent(id[0]);
		}

		protected void onPostExecute(Event result) {
			// Remove progress UI.
			if (refreshItem != null)
			    refreshItem.setActionView(null);
			refreshItem = null;

			// If the event can't be found, no UI refresh should occur.
			if (result == null) {
				Toast.makeText(getApplicationContext(),
                               "The event could not be found or no longer exists!",
                               Toast.LENGTH_LONG).show();
				return;
			}

			event = result;
			updateEventDetails();
		}
	}
}
