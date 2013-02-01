package com.appchallenge.android;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.DateFormat;

import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

//import com.appchallenge.android.APICalls;

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
    
    /**
     * The title of the event provided by the user.
     */
    private String title = "";
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEventTitle() {
		return this.title;
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
	private Date endDate;
	public void setDate(Date date) {
		if (startDate != null)
		    Log.d("setDate", "startDate was: " + startDate.getTime());
		this.startDate = date;
		Log.d("setDate", "startDate is now: " + startDate.getTime());
	}
	public Date getDate() {
		Log.d("The start", "date was gotten");
		return this.startDate;
	}
	
	public void setEndDate(Date date) {
		this.endDate = date;
	}
	
	public Date getEndDate() {
		Log.d("The end", "date was gotten");
		return this.endDate;
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
        Calendar c = Calendar.getInstance();
        if ((savedInstanceState != null) && savedInstanceState.containsKey("startDate") && savedInstanceState.containsKey("endDate")) {
        	this.startDate = new Date(savedInstanceState.getLong("startDate"));
        	this.endDate = new Date(savedInstanceState.getLong("endDate"));
        }
        else if (this.startDate == null && this.endDate == null) {
           	this.startDate = new Date();
           	c.add(Calendar.HOUR, 3);
            //this.endDate = c.getTime();
           	setEndDate(c.getTime());
            Log.d("Here3", this.endDate.toString());
        }
        // Set the map location to use the location given from the EventViewer.
        if (mapLocation == null) {
            Intent receivedIntent = getIntent();
            mapLocation = new LatLng(receivedIntent.getDoubleExtra("latitude", 0),
                                     receivedIntent.getDoubleExtra("longitude", 0));
        }
    }
    
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putLong("startDate", getDate().getTime());
    	
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.activity_create_event, menu);

        menu.findItem(R.id.action_back).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "submit" button to the action bar.
        int action = R.string.action_next;
        int id = R.id.action_next;
        if (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
        	action = R.string.action_finish;
        	id = R.id.action_submit;
        }

        MenuItem item = menu.add(Menu.NONE, id, Menu.NONE, action);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
    	
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
			// Advance to the next step in the wizard. If there is no
            // next step, setCurrentItem will do nothing.
			
			if(mPager.getCurrentItem() == 0 && (this.title == "")){
				Toast.makeText(context, "Enter a Valid Title.", duration).show();
				return true;
			}
			// TODO add error checking for valid times.
			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			return true;
		} else if (item.getItemId() == R.id.action_submit) {
			
			//
			
			
			// Prepare the date inputs to be in seconds.
			long startTime = this.startDate.getTime() / 1000;
			long endTime = this.endDate.getTime() / 1000;
			//long endTime = (startTime + (long)(this.duration * 60 * 60));
			Event newEvent = new Event(title, description, startTime, endTime, mapLocation);

			// Perform an asynchrounous API call to create the new event.
			createEventAPICaller apiCall = new createEventAPICaller();
			//Toast.makeText(context, newEvent.toJSON(), duration).show();
			apiCall.execute(newEvent);
			return true;
		}

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
    	// check if page 2 is open
    	    
    	    if(mPager.getCurrentItem() != 0){
    	    	mPager.setCurrentItem(mPager.getCurrentItem() - 1);
    	    }
    	    else{
    	        super.onBackPressed(); 
    	    }
    	}
    
    /**
     * Shows the time picker to allow changing the Event time.
     * @param v
     */
    public void showEventTimeDialog(View v) {
    	Log.d("showEvent", Integer.toString(v.getId()));
    	Log.d("Start ID", Integer.toString(R.id.event_start_button));
    	Log.d("end ID", Integer.toString(R.id.event_end_button));
    	switch(v.getId()){
    	case R.id.event_start_button:
            DialogFragment timePicker = new StartTimePicker();
            timePicker.show(getSupportFragmentManager(), "startTimePicker");
            break;
    	case R.id.event_end_button:
    		DialogFragment timePicker_2 = new EndTimePicker();
            timePicker_2.show(getSupportFragmentManager(), "endTimePicker");
            break;
    	}
    }
    @SuppressLint("ValidFragment")
	public class StartTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			Date startTime = ((CreateEventInterface)getActivity()).getDate();
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false);
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(((CreateEventInterface)getActivity()).getDate());
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
			((CreateEventInterface)getActivity()).setDate(cal.getTime());
			String time_2 = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
		    ((Button) findViewById(R.id.event_start_button)).setText(time_2);
		}
    }

    @SuppressLint("ValidFragment")
	public class EndTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			// Set the default time in on create
			Date endTime = ((CreateEventInterface) getActivity()).getEndDate();
			Calendar c = Calendar.getInstance();
			c.setTime(endTime);
			Log.d("Herehere", Integer.toString(c.get(Calendar.HOUR))+ " " + Integer.toString(c.get(Calendar.MINUTE)) + " " + endTime.toString());
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false);
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Calendar c = Calendar.getInstance();
			c.setTime(((CreateEventInterface)getActivity()).getEndDate());
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);
			((CreateEventInterface)getActivity()).setEndDate(c.getTime());
			String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
		    ((Button) findViewById(R.id.event_end_button)).setText(time);
		}
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
	 * Performs an asynchronous API call create a new event.
	 */
	private class createEventAPICaller extends AsyncTask<Event, Void, Event> {
		/**
	     * Informs the user that the event is being created.
	     */
	    ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// Set up progress indication.
			dialog = ProgressDialog.show(CreateEvent.this, "Creating...", "");
		}

		@Override
		protected Event doInBackground(Event... event) {
			return APICalls.createEvent(event[0]);
		}

		@Override
		protected void onPostExecute(Event result) {
			// Close the wizard and any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Could not create event!", Toast.LENGTH_LONG)).show();
				CreateEvent.this.finish();
				return;
			}
			
			// Add the event to the event viewer.
			
			CreateEvent.this.finish();
		}
	}
}
