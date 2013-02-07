package com.appchallenge.android;

import java.util.Date;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EventDetails extends SherlockFragmentActivity implements EventDetailsInfoTab.InfoTabListener,
                                                                      EventDetailsAttendanceTab.AttendanceTabListener,
                                                                      EventDetailsLocationTab.LocationTabListener {
	private int id;
	
	// Methods implementing InfoTabListener
	private String title;
	public String getEventTitle() {
		return this.title;
	}
	
	private String description;
	public String getEventDescription() {
		return this.description;
	}

	private Date startDate;
	public Date getEventStartDate() {
		return this.startDate;
	}

	private Date endDate;
	public Date getEventEndDate() {
		return this.endDate;
	}
	
	// Methods implementing AttendanceTabListener
	private int attendance;
	public int getEventAttendance() {
		return this.attendance;
	}
	
	// Methods implementing LocationTabListener
	private LatLng location;
	public LatLng getEventLocation() {
		return this.location;
	}

	private LatLng userLocation;
	public LatLng getUserLocation() {
		return this.userLocation;
	}

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

		// Establish the tab navigation interface.
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab attendTab = bar.newTab().setText("Attendance");
		Tab infoTab = bar.newTab().setText("Info");
		Tab locationTab = bar.newTab().setText("Location");

		Fragment attendFragment = new EventDetailsAttendanceTab();
		Fragment infoFragment = new EventDetailsInfoTab();
		Fragment locationFragment = new EventDetailsLocationTab();

		attendTab.setTabListener(new TabsListener(attendFragment));
		infoTab.setTabListener(new TabsListener(infoFragment));
		locationTab.setTabListener(new TabsListener(locationFragment));

		bar.addTab(attendTab);
		bar.addTab(infoTab);
		bar.addTab(locationTab);
		
		if (savedInstanceState != null)
			bar.selectTab(bar.getTabAt(savedInstanceState.getInt("currentTab", 1)));
		else
		    bar.selectTab(infoTab);
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
	
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Prevent the current tab from being lost on rotation
		savedInstanceState.putInt("currentTab", getSupportActionBar().getSelectedTab().getPosition());
		
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * A basic implementation of a TabListener which allows switching between tabs.
	 */
	class TabsListener implements ActionBar.TabListener {
	    public Fragment fragment;

	    public TabsListener(Fragment fragment) {
	        this.fragment = fragment;
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {}

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        ft.replace(R.id.details_fragment_container, fragment);
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        ft.remove(fragment);
	    }
	}
}
