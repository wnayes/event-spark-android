package com.appchallenge.android;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

public class EventDetails_2 extends SherlockFragmentActivity {

	private Event event;
	private LatLng userLocation;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_2);
        
		Intent intent = getIntent();
		this.event = intent.getParcelableExtra("event");
		this.userLocation = new LatLng(intent.getDoubleExtra("userLatitude", 0),
				                       intent.getDoubleExtra("userLongitude", 0));
		
		// This network operation was called on the main thread.
		//this.attendance = APICalls.getAttendance(this.id);
		Log.d("this", Integer.toString(this.event.getAttendance()));
		((TextView) findViewById(R.id.title)).setText(this.event.getTitle());
		((TextView) findViewById(R.id.description)).setText(this.event.getDescription());
		((TextView) findViewById(R.id.attending)).setText("Attending: " + Integer.toString(this.event.getAttendance()));
		((TextView) findViewById(R.id.end_date)).setText("End Date: " + this.event.getEndDate().toString());
		
		
		Button button = (Button) findViewById(R.id.get_directions);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Prepare maps url query url parameters.
            	LatLng startLoc = userLocation;
            	LatLng endLoc = event.getLocation();
            	String startCoords = ((Double)startLoc.latitude).toString() + "," + ((Double)startLoc.longitude).toString();
            	String endCoords = ((Double)endLoc.latitude).toString() + "," + ((Double)endLoc.longitude).toString();
            	String url = "http://maps.google.com/maps?saddr=" + startCoords + "&daddr=" + endCoords;
            	Log.d("EventDetailsLocationTab", "Get directions, " + url);

                // Pass an intent to an activity that can provide directions.
            	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
		
		Log.d("id", Integer.toString(this.event.getId()));
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
	      case R.id.menu_refresh_events:
	    	 APICalls.getAttendance(this.event.getId());
	    	 return true;
	      default:
	         return super.onOptionsItemSelected(item);
	   }
	}
	
	@Override
    public void onBackPressed() {
        // Have the back button go back one page instead of exit.
        finish();
    }
	
	
	public void joinEvent (View v) {
		loadAttendeesCaller attendees = new loadAttendeesCaller();
		attendees.execute(this.event.getId());
		int join = 0;
		try {
			join = attendees.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		if (join == 0) {
			Toast.makeText(context, "Failed to join event", duration).show();
		}
		else {
			// Main thread networking call.
		    //this.attendance = APICalls.getAttendance(this.id);
		    ((TextView) findViewById(R.id.attending)).setText("Attending: " + Integer.toString(this.event.getAttendance()));
		}
		
	}
	
	private class loadAttendeesCaller extends AsyncTask<Integer, Integer, Integer> {
		/**
	     * Informs the user that the event is being created.
	     */
	    ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// Set up progress indication.
			dialog = ProgressDialog.show(EventDetails_2.this, "Finding Attendees...", "");
		}

		@Override
		protected Integer doInBackground(Integer... attend) {
			return APICalls.joinAttendance(attend[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			// Close the wizard and any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Could load attending!", Toast.LENGTH_LONG)).show();
				return;
			}
		}
	}
}
