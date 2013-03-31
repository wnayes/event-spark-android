package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.appchallenge.android.ReportDialogFragment.ReportDialogListener;
import com.appchallenge.android.ReportDialogFragment.ReportReason;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.google.android.gms.maps.model.LatLng;

public class EventDetails extends SherlockFragmentActivity implements ReportDialogListener {
	// Private members containing the Event information.
	private Event event;
	private LatLng userLocation;
	private Integer id;
	private String URL = "http://saypoint.dreamhosters.com/facebook/";
	
	/**
	 * Indicates whether we have attended the event already.
	 */
	private Boolean attended;

    /**
     * Provides access to our local sqlite database.
     */
    private LocalDatabase localDB;

    /**
     * Used to provide the sharing feature in the action bar.
     */
    private ShareActionProvider mShareActionProvider;

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
		
		id = this.event.getId();
		URL = URL + id.toString() + ".html";
		
		// Learn whether we have already attended this event.
		if (attended == null) {
			if (localDB == null)
	    		localDB = new LocalDatabase(this);
			attended = localDB.getAttendanceStatus(event.getId());
		}

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
	    this.updateAttendingText(this.event.getAttendance());
	    
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

	/**
	 * Separate method for updating the attending count. Allows us to set the
	 * number without mutating the private event object.
	 * @param attendingCount The number of users attending.
	 */
	private void updateAttendingText(int attendingCount) {
		TextView attendingTextBox = ((TextView)findViewById(R.id.event_details_attendance));

		// Change the string and image based on whether we are attending.
		String attending;
		Drawable icon;
		if (this.attended) {
			// Prevent the count from going negative.
			int count = attendingCount - 1 < 0 ? 0 : attendingCount - 1;

			attending = getResources().getQuantityString(R.plurals.users_you_attending, count, count);
			icon = getResources().getDrawable(R.drawable.people);
		}
		else {
			attending = getResources().getQuantityString(R.plurals.users_attending, attendingCount, attendingCount);
			icon = getResources().getDrawable(R.drawable.person);
		}
		attendingTextBox.setText(attending);
		attendingTextBox.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
	}

	private Menu _menu;
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_event_details, menu);

		// Establish the "Share" action provider.
        //this.mShareActionProvider = (ShareActionProvider)menu.findItem(R.id.share).getActionProvider();
        //Intent intent = this.getEventShareIntent();
        //if (intent != null && this.mShareActionProvider != null)
          //  this.mShareActionProvider.setShareIntent(intent);

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
	        	// Show dialog allowing the user to report an event.
				DialogFragment reportDialog = new ReportDialogFragment();
				reportDialog.show(getSupportFragmentManager(), "reportDialog");
	        	return true;
            case R.id.menu_attend_event:
                // Commit to attending the event.
            	new attendEventAPICaller().execute(this.event.getId());
	        	return true;
            case R.id.share:
            	shareEvent();
            	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	   }
	}
	

	/**
     *Returns a Share intent for use with the Share action provider.
     */
    private void shareEvent() {
    	Log.d("EventDetails","Started Posting the Event");
    	Session session = Session.getActiveSession();
    	if (session == null) {
    		Toast.makeText(this.getApplicationContext(), "Log into Facebook to share the event", Toast.LENGTH_LONG).show();
    		return;
    	}
        
    }

	/**
	 * Receives the ReportReason from the report dialog and submits the report.
	 */
	public void onReportDialogOKClick(DialogFragment dialog, ReportReason reason) {
        new reportEventAPICaller().execute(this.event.getId(), reason.ordinal());
	}
	
	/**
	 * Performs an async call to report an event for a violation.
	 */
	private class reportEventAPICaller extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			// TODO: Establish progress UI changes?
		}

		@Override
		protected String doInBackground(Integer... values) {
			// Values contains [0] id, [1] ReportReason
			return APICalls.reportEvent(values[0], values[1], Identity.getUserId(getApplicationContext()));
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO: Remove progress UI.

			// Some sort of error occurred during the request.
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Could not submit report. Please try again!", Toast.LENGTH_LONG)).show();
				return;
			}

			// Our request went through and we have not previously reported the event.
			if (result.equals("OK")) {
				// Display a message thanking the user for the report.
				AlertDialog.Builder builder = new AlertDialog.Builder(EventDetails.this);
                builder.setMessage("Thank you for submitting a report.");
                builder.setPositiveButton(R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			
			// The user has already submitted a report.
			else if (result.equals("PREVIOUSLY_REPORTED")) {
				(Toast.makeText(getApplicationContext(), "You have already submitted a report. We will investigate the event as soon as possible.", Toast.LENGTH_LONG)).show();
			}
		}
	}

	/**
	 * Performs an async call to submit attendance to an event.
	 */
	private class attendEventAPICaller extends AsyncTask<Integer, Void, String> {
		/**
		 * Quick access to the attend button in the actionbar.
		 */
		MenuItem attendItem;

		@Override
		protected void onPreExecute() {
			// Establish progress UI changes.
			if (_menu != null) {
				attendItem = _menu.findItem(R.id.menu_attend_event);
				if (attendItem != null)
					attendItem.setActionView(R.layout.actionbar_refresh_progress);
			}
		}

		@Override
		protected String doInBackground(Integer... id) {
			return APICalls.attendEvent(id[0], Identity.getUserId(getApplicationContext()));
		}

		@Override
		protected void onPostExecute(String result) {
			// Remove progress UI.
			if (attendItem != null)
				attendItem.setActionView(null);
			attendItem = null;

			// Some sort of error occurred during the request.
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Could not submit attendance request. Please try again!", Toast.LENGTH_LONG)).show();
				return;
			}

			// Our request went through and we have not yet attended previously.
			if (result.equals("OK")) {
				// Remember this action in our local database.
				attended = true;
				if (localDB == null)
				    localDB = new LocalDatabase(EventDetails.this);
				localDB.trackAttendance(event.getId());

				// Update the text display to reflect the changed number.
				updateAttendingText(event.getAttendance() + 1);
				(Toast.makeText(getApplicationContext(), "Thanks for attending!", Toast.LENGTH_LONG)).show();
			}
			
			// The user has already said they will attend the event.
			else if (result.equals("PREVIOUSLY_ATTENDED")) {
				(Toast.makeText(getApplicationContext(), "You have already indicated you will attend.", Toast.LENGTH_LONG)).show();
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
	
	private interface EventGraphObject extends GraphObject {
	    // A URL
	    public String getUrl();
	    public void setUrl(String url);

	    // An ID
	    public String getId();
	    public void setId(String id);
	}
	
	private interface EatAction extends OpenGraphAction {
	    // The meal object
	    public EventGraphObject getMeal();
	    public void setMeal(EventGraphObject meal);
	}
	
	private static final String POST_ACTION_PATH = "me/appchallenge_arrows:join";
	AsyncTask<Void, Void, Response> task = 
		    new AsyncTask<Void, Void, Response>() {

		    @Override
		    protected Response doInBackground(Void... voids) {
		        // Create an eat action
		         EventGraphObject eatAction = GraphObject.Factory.create(EventGraphObject.class);
		        // Populate the action with the POST parameters:
		        // the meal, friends, and place info
		        //for (BaseListElement element : listElements) {
		           // element.populateOGAction(eatAction);
		        //}   
		        // Set up a request with the active session, set up
		        // an HTTP POST to the eat action endpoint
		        Request request = new Request(Session.getActiveSession(),
		                POST_ACTION_PATH, null, HttpMethod.POST);
		        // Add the post parameter, the eat action
		        request.setGraphObject(eatAction);
		        // Execute the request synchronously in the background
		        // and return the response.
		        return request.executeAndWait();
		    }   

		    @Override
		    protected void onPostExecute(Response response) {
		        // When the task completes, process
		        // the response on the main thread
		        //onPostActionResponse(response);
		     }   
		};  
}
