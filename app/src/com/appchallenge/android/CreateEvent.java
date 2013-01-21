package com.appchallenge.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

import com.appchallenge.android.APICalls;

/**
 * Wizard activity for creating new events.
 */
public class CreateEvent extends SherlockFragmentActivity {
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    
    private String name = "empty";
    private String type = "tempty";
    private String desc = "dempty";
    
    
    public void onCheckboxClicked(View view) {
    	
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Sherlock);
        setContentView(R.layout.activity_create_event);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager)findViewById(R.id.pager);
        mPagerAdapter = new CreateEventPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.activity_create_event, menu);

        menu.findItem(R.id.action_back).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "finish" button to the action bar.
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                        ? R.string.action_finish
                        : R.string.action_next);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, "test", duration);
    	
        if (item.getItemId() == android.R.id.home) {
			// See http://developer.android.com/design/patterns/navigation.html for more.
			NavUtils.navigateUpTo(this, new Intent(this, EventViewer.class));
        	return true;
        	
		} else if (item.getItemId() == R.id.action_back) {
			// Go to the previous step in the wizard. If there is no previous step,
			// setCurrentItem will do nothing.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
			return true;
		
		} else if ((item.getItemId() == R.id.action_next) && !(item.getTitle().toString().equals("Submit"))) {
			// Advance to the next step in the wizard. If there is no next step, setCurrentItem
			// will do nothing.
			
			/**
			 * This if statement is used to save the data from the first wizard page to be 
			 * accessed by the constructor below.  For reasons I am currently uncertain about
			 * When trying to access event_name from the 3rd wizard page (or trying to access
			 * and field on the first page from the 3rd page) the findViewById returned null
			 * So by putting the call here and using it only when switching from the first page
			 * we can save the values to some local variables and use them in future calls
			 */
			if (mPager.getCurrentItem() == 0){
			name = ((EditText) findViewById(R.id.event_name)).getText().toString();
			type = ((Spinner) findViewById(R.id.spinner1)).getSelectedItem().toString();
			desc = ((EditText) findViewById(R.id.event_description)).getText().toString();
			}
			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			return true;
		
		} else if (item.getItemId() == R.id.action_next && item.getTitle().toString().equals("Submit")) {
			//This is the code that extracts the data from the wizard
			//Cannot get data from first page see above.
			//It will then make a JSONObject and Post it returning a toast about success or failure
			
			/**
			 * Basic Checking for making sure requirements are met.
			 */
			if (name.length() > 250 || desc.length() > 1000){
				toast = Toast.makeText(context, "Your Title or Lenght Exceed Maximum Limits" +
						"Max Title: 250 Characters, Max Description: 1000 Characters", duration);
				toast.show();
				return true;
			}
			
		    /**
		     * Creates Time of the form HHMMAAHHMMAA where the first HH is the start time the second
			 * HH is the end time and the same things for minutes.  The AA is for setting am versus pm.
			 * 01 is am and 02 is pm.
			 * TODO Update web script to accept new time input
			 **/
			String am_pm_start = ((Spinner) findViewById(R.id.spinner_am_pm_start)).getSelectedItem().toString();
			String am_pm_end = ((Spinner) findViewById(R.id.spinner_am_pm_end)).getSelectedItem().toString();
			am_pm_start = (am_pm_start.equals("am")) ? "01" : (am_pm_start.equals("pm")) ? "02" : null;
			am_pm_end = (am_pm_end.equals("am")) ? "01" : (am_pm_end.equals("pm")) ? "02": null;
			
			String time = ((Spinner) findViewById(R.id.spinner_hours_start)).getSelectedItem().toString()
					+ ((Spinner) findViewById(R.id.spinner_minutes_start)).getSelectedItem().toString()
					+ am_pm_start
					+ ((Spinner) findViewById(R.id.spinner_hours_end)).getSelectedItem().toString()
					+ ((Spinner) findViewById(R.id.spinner_minutes_end)).getSelectedItem().toString()
					+ am_pm_end;
			
			
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			String loc = LocationManager.GPS_PROVIDER;
			Location lastKnownLocation = locationManager.getLastKnownLocation(loc);
			LatLng location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
			JSONObject object = new JSONObject();
			Event newEvent = new Event(name, desc, time, type, location);
			
			try {
                object.put("title", name);
                object.put("description", desc);
                object.put("time", time);
                object.put("type", type);
                object.put("latitude", lastKnownLocation.getLatitude());
                object.put("longitude", lastKnownLocation.getLongitude());
                String object_2 = object.toString();
                toast = Toast.makeText(context, object_2, duration);
				
				//This toast verifies that the data is being passed correctly
				//Remove after done debugging
				//toast.show();
			} catch (JSONException e) {
			   	Log.e(CreateEvent.class.toString(), "Error creating JSON Object in Create Event");
			    e.printStackTrace();
			}
			boolean returnedEvent = APICalls.createEvent(newEvent);
			String text = "false";
			if(returnedEvent){
			    text = "true";
			}
			toast = Toast.makeText(context, text, duration);
			toast.show();
			    // TODO Add the actual Call to the database
	
			return true;
		}

        return super.onOptionsItemSelected(item);
    }

	/**
     * A pager adapter that represents the wizard pages sequentially.
     */
    public class CreateEventPagerAdapter extends FragmentStatePagerAdapter {
    	// Instances of each wizard page, in order.
    	public Fragment[] wizardPages = new Fragment[] {
            new CreateEventPage1EventName(),
            new CreateEventPage2EventTime(),
            new CreateEventPage3EventLocation()
    	};
    	
        public CreateEventPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return wizardPages[position];
        }

        @Override
        public int getCount() {
            return wizardPages.length;
        }
    }
}
