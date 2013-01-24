package com.appchallenge.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLng;

import com.appchallenge.android.APICalls;

/**
 * Wizard activity for creating new events.
 */
public class CreateEvent extends SherlockFragmentActivity implements CreateEventInterface {
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    //private String type = "tempty";

    // Methods for the CreateEventInterface, which provide access to wizard public members
    // for the fragment pages.
    // See http://developer.android.com/training/basics/fragments/communicating.html 

    /**
     * The name of the event provided by the user.
     */
    private String name = "";
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}

	/**
	 * The description of the event provided by the user.
	 */
    private String description = "";
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return this.description;
	}

	/**
	 * The date when the event goes live.
	 */
	private Date startDate;
	public void setDate(Date date) {
		this.startDate = date;
	}
	public Date getDate() {
		return this.startDate;
	}

	/**
     * Location keeping track of the wizard map camera.
     */
    private LatLng mapLocation;
	public void setLocation(LatLng location) {
		this.mapLocation = location;
	}
	public LatLng getLocation() {
		return this.mapLocation;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // but for simplicity, the activity provides the actions.
                invalidateOptionsMenu();
            }
        });
        
        if (startDate == null)
        	startDate = new Date();
        
        // Set the map location to use the location given from the EventViewer.
        if (mapLocation == null) {
            Intent receivedIntent = getIntent();
            mapLocation = new LatLng(receivedIntent.getDoubleExtra("latitude", 0),
                                     receivedIntent.getDoubleExtra("longitude", 0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.activity_create_event, menu);

        menu.findItem(R.id.action_back).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "submit" button to the action bar.
        int action = (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                     ? R.string.action_finish : R.string.action_next;
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE, action);
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

		} else if (item.getItemId() == R.id.action_next) {
			// Advance to the next step in the wizard. If there is no next step, setCurrentItem
			// will do nothing.
			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			return true;
		
		} else if (item.getItemId() == R.id.action_finish) {
			// This is the code that extracts the data from the wizard
			// It will then make a JSONObject and Post it returning a toast about success or failure
			
			//Basic Checking for making sure requirements are met.
			if (name.length() > 250 || description.length() > 1000) {
				toast = Toast.makeText(context, "Your Title or Lenght Exceed Maximum Limits" +
						"Max Title: 250 Characters, Max Description: 1000 Characters", duration);
				toast.show();
				return true;
			}

			long time = new java.util.Date().getTime();
			if (this.startDate != null)
				time = this.startDate.getTime();
			
			Event newEvent = new Event(name, description, time, /*type,*/ mapLocation);
			
			JSONObject object = new JSONObject();
			try {
                object.put("title", name);
                object.put("description", description);
                object.put("time", time);
                //object.put("type", type);
                object.put("latitude", mapLocation.latitude);
                object.put("longitude", mapLocation.longitude);
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
			toast = Toast.makeText(context, ((Boolean)returnedEvent).toString(), duration);
			toast.show();
			
			// TODO Add the actual Call to the database
	
			// TODO This return is not enough. We will need to wait until the API call
			// finishes before closing this activity. A progress spinner will probably
			// be the cleanest way to show progress.
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
    
    /**
     * Shows the time picker to allow changing the Event time.
     * @param v
     */
    public void showEventTimeDialog(View v) {
        DialogFragment timePicker = new EventTimePicker();
        timePicker.show(getSupportFragmentManager(), "timePicker");
    }
    public class EventTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Calendar c = Calendar.getInstance();
			c.setTime(startDate);
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);
			startDate = c.getTime();
			String time = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(startDate);
		    ((TextView)findViewById(R.id.event_time_display)).setText(time);
		}
    }

    /**
     * Shows the date picker to allow changing the Event date.
     * @param v
     */
    public void showEventDateDialog(View v) {
        DialogFragment datePicker = new EventDatePicker();
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }
    public class EventDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
			
			// Set the date limit to today and tomorrow.
			dialog.getDatePicker().setMinDate(new Date().getTime() - 3000);
			dialog.getDatePicker().setMaxDate(new Date().getTime() + 86400000);
			
			return dialog;
        }
		
		// Update the saved date and UI.
		public void onDateSet(DatePicker view, int year, int month, int day) {
			Calendar c = Calendar.getInstance();
			c.setTime(startDate);
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);
			startDate = c.getTime();

			String date = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(startDate);
			((TextView)findViewById(R.id.event_date_display)).setText(date);
        }
    }
}
