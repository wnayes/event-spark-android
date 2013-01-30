package com.appchallenge.android;

import java.util.Date;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class EventDetails extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		
		// The home button takes the user back to the map display.
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);

		// Receive Event information to display via Intent.
		Intent intent = getIntent();
		Integer id = intent.getIntExtra("id", -1);
		String title = intent.getStringExtra("title");
		String description = intent.getStringExtra("description");
		Long startDate = intent.getLongExtra("startDate", new Date().getTime() / 1000);
		Long endDate = intent.getLongExtra("endDate", new Date().getTime() / 1000);
		Double latitude = intent.getDoubleExtra("latitude", 0);
		Double longitude = intent.getDoubleExtra("longitude", 0);
		
		// Update the display to show the user the Event information.
		((TextView)findViewById(R.id.event_details_title)).setText(title);
		((TextView)findViewById(R.id.event_details_description)).setText(description);
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
	      default:
	         return super.onOptionsItemSelected(item);
	   }
	}
}
