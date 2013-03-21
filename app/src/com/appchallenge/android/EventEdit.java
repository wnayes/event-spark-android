package com.appchallenge.android;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.appchallenge.android.Event.Type;
import com.google.android.gms.maps.model.LatLng;


public class EventEdit extends SherlockFragmentActivity implements CreateEventInterface{
	
	String title;
	@Override
	public String getEventTitle() {
		if (title != null)
			return this.title;
		return "";
	}

	@Override
	public void setTitle(String name) {
		this.title = name;
	}

	String desc;
	@Override
	public String getDescription() {
		if (desc != null)
			return this.desc;
		return "";
	}

	@Override
	public void setDescription(String description) {
		this.desc = description;		
	}

	Date startDate;
	@Override
	public Date getStartDate() {
		return this.startDate;
	}

	@Override
	public void setStartDate(Date date) {
		this.startDate = date;
	}

	Date endDate;
	@Override
	public Date getEndDate() {
		return this.endDate;
	}

	@Override
	public void setEndDate(Date date) {
		this.endDate = date;
	}

	@Override
	public LatLng getLocation() {return null;/* Not used */	}
	@Override
	public void setLocation(LatLng location) {/* Not used*/	}

	Type type;
	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;		
	}

	int id;
	protected static Activity activity;
	LocalDatabase localDB;
	ArrayList<Event> currentEvents;
	int state = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activity = this;
        
        // The home button takes the user back to the map display.
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
        if (state == 1) {
        	onEdit(null);
        	return;
        }
        
        int eventCount = 0;
		String ownerId = "";
		        
        Intent intent = getIntent();
        currentEvents = intent.getParcelableArrayListExtra("currentEvents");
        
        //Creates the ViewGroup for the base of the view
        ViewGroup view = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(R.layout.event_edit_dialog, null);
		ScrollView root = (ScrollView) view.findViewById(R.id.event_edit_dialog);
		LinearLayout list = new LinearLayout(getApplicationContext());
		list.setOrientation(LinearLayout.VERTICAL);
		
		TextView title = new TextView(getApplicationContext());
		title.setText("Click an Event to Edit It.");
		title.setTextSize(25);
		title.setTextColor(Color.BLACK);
		title.setPadding(10, 10, 10, 20);
		list.addView(title);
		
		if (localDB == null)
			localDB = new LocalDatabase(getApplicationContext());
		
		//Populates the list with all the events you own
		for (final Event event : currentEvents) {
			
			ownerId = localDB.getEventSecretId(event);
						
			if (ownerId != null && ownerId.length() > 0) {
			    TextView text = new TextView(getApplicationContext());
			    text.setText(event.getTitle());
			    text.setTextSize(20);
			    text.setPadding(10, 0, 10, 10);
			    text.setTextColor(Color.BLACK);
			    text.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onEdit(event);
					}
				});
			    list.addView(text);
			    eventCount++;
			}
		}
		
		//If you own no events displays no events found
		if (eventCount == 0 || currentEvents == null) {
			TextView text = new TextView(getApplicationContext());
			text.setText("No Events Found.");
			text.setTextSize(30);
			text.setTextColor(Color.BLACK);
			list.addView(text);
		}
		root.addView(list);
		setContentView(root);
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState) {
    	if (currentEvents != null) {
    		savedInstanceState.putParcelableArrayList("currentEvents", this.currentEvents);
    		
    	}
        super.onSaveInstanceState(savedInstanceState);
    }
	
	@Override
    protected void onPause() {
    	super.onPause();
     	// Close our database helper if necessary.
    	if (localDB != null)
            localDB.close();
    }
	
	@Override
	protected void onStop() {
		state = 0;
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		    getSupportMenuInflater().inflate(R.menu.event_edit, menu);
            return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == android.R.id.home){
			NavUtils.navigateUpTo(this, new Intent(this, EventViewer.class));
        	return true;
		} else if (item.getItemId() == R.id.event_edit_submit) {
			if (state == 0) {
				Toast.makeText(getApplicationContext(), "No Event Selected", Toast.LENGTH_SHORT).show();
				return true;
			}
			Event tempEvent = new Event(id, title, type, desc, this.startDate, this.endDate, null);
			updateEventAPICaller updateEvent = new updateEventAPICaller();
			updateEvent.execute(tempEvent);
			return true;
		} else if (item.getItemId() == R.id.event_edit_delete) {
			if (state == 0) {
				Toast.makeText(getApplicationContext(), "No Event Selected", Toast.LENGTH_SHORT).show();
				return true;
			}
			Event tempEvent = new Event(id, title, type, desc, this.startDate, this.endDate, null);
			deleteEventAPICaller deleteEvent = new deleteEventAPICaller();
			deleteEvent.execute(tempEvent);
			//To-Do add network call
			return true;
		} else if (item.getItemId() == R.id.action_back) {
			state = 0;
			Log.d("EventEdit", "State has changed from 1 to 0");
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onEdit(Event event) {
		
		    //Changes the state so the class knows if we have selected and event on creation
		    // Also wipes and creates the new ui
		    if (state != 1) {
		        ScrollView oldView = (ScrollView) findViewById(R.id.event_edit_dialog);
		        ViewGroup view = (ViewGroup) oldView.getParent();
		        view.removeView(oldView);
		        setContentView(R.layout.event_edit_layout);
		        state = 1;
		        Log.d("EventEdit", "State has changed from 0 to 1");
		    }
		    //Set id
		    id = event.getId();
		    
		    //Set the title and description
			EditText title = (EditText) findViewById(R.id.event_edit_title);
			EditText description = (EditText) findViewById(R.id.event_edit_description);
			if (event != null) {
				Log.d("Passed Null", event.toJSON());
			    title.setText(event.getTitle());
			    this.setTitle(event.getTitle());
			    description.setText(event.getDescription());
			    this.setDescription(event.getDescription());
			} else {
				title.setText(this.getTitle());
				description.setText(this.getDescription());
			}
			
			//Set type;
			Spinner typeSpinner = (Spinner)findViewById(R.id.event_edit_type_spinner);
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
	                                                                             R.array.type_array,
	                                                                             android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        typeSpinner.setAdapter(adapter);
	        if (event != null) {
	            typeSpinner.setSelection(event.getType().getValue());
	            setType(event.getType());
	        } else {
	        	typeSpinner.setSelection(this.getType().getValue());
	        }
	        
	        //Setting the Dates
	        //Buttons
	        if (event != null) {
	            setStartDate(event.getStartDate());
	            setEndDate(event.getEndDate());
	        }
	        String startString = DateFormat.getTimeInstance(DateFormat.SHORT).format(startDate);
	        ((Button)findViewById(R.id.event_edit_start_button)).setText(startString);
	        String endString = DateFormat.getTimeInstance(DateFormat.SHORT).format(endDate);
	        ((Button)findViewById(R.id.event_edit_end_button)).setText(endString);
			
	        //Spinners
	        Log.d("Here", "test");
	        Spinner startSpinner = (Spinner)findViewById(R.id.event_edit_start_spinner);
	        Spinner endSpinner = (Spinner)findViewById(R.id.event_edit_end_spinner);
	        ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
	                                                                             R.array.relative_days,
	                                                                             android.R.layout.simple_spinner_item);
	        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        startSpinner.setAdapter(dateAdapter);
	        endSpinner.setAdapter(dateAdapter);
	        
	        Calendar startCalendar = Calendar.getInstance();
	        startCalendar.setTime(startDate);
	        Calendar endCalendar = Calendar.getInstance();
	        endCalendar.setTime(endDate);
	        Calendar today = Calendar.getInstance();
	     // Select the correct day spinner values.
	        if (startCalendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR))
	        	startSpinner.setSelection(1);
	        else
	        	startSpinner.setSelection(0);
	        if (endCalendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR))
	            endSpinner.setSelection(1);
	        else
	            endSpinner.setSelection(0);

	        // Watch for changes to the date spinners.
	        startSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	            	Date startDate = ((CreateEventInterface)EventEdit.activity).getStartDate();
	            	Calendar startCalendar = Calendar.getInstance();
	            	startCalendar.setTime(startDate);

	            	Log.d("startSpinner.setOnItemSelectedListener", "pos " + position + "  id " + (Long)id);

	            	int todayVal = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
	            	int tomorrowVal = todayVal + 1;
	            	if (startCalendar.get(Calendar.DAY_OF_YEAR) == todayVal && position == 1)
	            		startCalendar.add(Calendar.DATE, 1);
	            	else if (startCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowVal && position == 0)
	            		startCalendar.add(Calendar.DATE, -1);
	            	else
	            		return;
		
	            	((CreateEventInterface)EventEdit.activity).setStartDate(startCalendar.getTime());
	            }
	            // Interface requirements
	            public void onNothingSelected(AdapterView<?> parentView) {}
	        });
	        endSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	            	Date endDate = ((CreateEventInterface)EventEdit.activity).getEndDate();
	            	Calendar endCalendar = Calendar.getInstance();
	            	endCalendar.setTime(endDate);

	            	Log.d("endSpinner.setOnItemSelectedListener", "pos " + position + "  id " + (Long)id);

	            	int todayVal = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
	            	int tomorrowVal = todayVal + 1;
	            	if (endCalendar.get(Calendar.DAY_OF_YEAR) == todayVal && position == 1)
	            		endCalendar.add(Calendar.DATE, 1);
	            	else if (endCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowVal && position == 0)
	            		endCalendar.add(Calendar.DATE, -1);
	            	else
	            		return;

	            	((CreateEventInterface)EventEdit.activity).setEndDate(endCalendar.getTime());
	            }
	            // Interface requirements
	            public void onNothingSelected(AdapterView<?> parentView) {}
	        });

		
	}
	
	public void showEventTimeDialog(View v) {
    	DialogFragment timePicker;
    	switch (v.getId()) {
	    	case R.id.event_start_button:
	            timePicker = new StartTimePicker();
	            timePicker.show(getSupportFragmentManager(), "startTimePicker");
	            break;
	    	case R.id.event_end_button:
	    		timePicker = new EndTimePicker();
	    		timePicker.show(getSupportFragmentManager(), "endTimePicker");
	            break;
    	}
    }

	
	public class StartTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			Date startTime = ((CreateEventInterface)getActivity()).getStartDate();
			Calendar c = Calendar.getInstance();
			c.setTime(startTime);
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Create a new Date object with the updated time.
			Calendar cal = Calendar.getInstance();
			cal.setTime(((CreateEventInterface)getActivity()).getStartDate());
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);

			// Send this updated date back to the wizard activity.
			Date newDate = cal.getTime();
			((CreateEventInterface)getActivity()).setStartDate(newDate);

			// Update the display text.
			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
		    ((Button) findViewById(R.id.event_edit_start_button)).setText(timeString);
		}
    }

	
	public class EndTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Date endTime = ((CreateEventInterface) getActivity()).getEndDate();
			Calendar c = Calendar.getInstance();
			c.setTime(endTime);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Create a new Date object with the updated time.
			Calendar c = Calendar.getInstance();
			c.setTime(((CreateEventInterface)getActivity()).getEndDate());
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);

			// Send this updated date back to the wizard activity.
			Date newDate = c.getTime();
			((CreateEventInterface)getActivity()).setEndDate(newDate);

			// Update the display text.
			String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate);
		    ((Button) findViewById(R.id.event_edit_end_button)).setText(timeString);
		}
    }
	
	private class updateEventAPICaller extends AsyncTask<Event, Void, Event> {
		/**
	     * Informs the user that the event is being created.
	     */
	    ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// Set up progress indication.
			dialog = ProgressDialog.show(EventEdit.this, "Updating...", "");
		}

		@Override
		protected Event doInBackground(Event... event) {
			return APICalls.updateEvent(event[0], Identity.getUserId(getApplicationContext()), "update");
		}

		@Override
		protected void onPostExecute(Event result) {
			// Close the wizard and any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Could not update event!", Toast.LENGTH_LONG)).show();
				EventEdit.this.finish();
				return;
			}
			
			// Pass the new event to the event viewer.
			Intent intent = new Intent(EventEdit.this, EventViewer.class);
			intent.putExtra("event", result);
		    EventEdit.this.setResult(RESULT_OK, intent);
			EventEdit.this.finish();
		}
	}

	private class deleteEventAPICaller extends AsyncTask<Event, Void, Event> {
		/**
	     * Informs the user that the event is being created.
	     */
	    ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// Set up progress indication.
			dialog = ProgressDialog.show(EventEdit.this, "Deleteing...", "");
		}

		@Override
		protected Event doInBackground(Event... event) {
			return APICalls.updateEvent(event[0], Identity.getUserId(getApplicationContext()), "delete");
		}

		@Override
		protected void onPostExecute(Event result) {
			// Close the wizard and any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == null) {
				(Toast.makeText(getApplicationContext(), "Could not delete event!", Toast.LENGTH_LONG)).show();
				EventEdit.this.finish();
				return;
			}
			
			// Pass the new event to the event viewer.
			Intent intent = new Intent(EventEdit.this, EventViewer.class);
			intent.putExtra("event", result);
		    EventEdit.this.setResult(RESULT_OK, intent);
			EventEdit.this.finish();
		}
	}

}