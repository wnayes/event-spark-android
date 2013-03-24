package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.appchallenge.android.Event.Type;
import com.google.android.gms.maps.model.LatLng;


@SuppressLint("ValidFragment")
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
		return startDate;
	}

	@Override
	public void setStartDate(Date date) {
		startDate = date;
	}

	Date endDate;
	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date date) {
		endDate = date;
	}

	LatLng location;
	@Override
	public LatLng getLocation() {
		return location;
	}
	@Override
	public void setLocation(LatLng position) {
		location = position;
	}

	Type type;
	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type inType) {
		type = inType;		
	}

	protected int id;
	protected int attending;
	protected static Activity activity;
	LocalDatabase localDB;
	protected Event event;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_edit_layout);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
		activity = this;
		
		Intent intent = getIntent();
		event = intent.getParcelableExtra("event");
	    
		//Set id
	    id = event.getId();
	    attending = event.getAttendance();
	    this.setLocation(event.getLocation());
	    //Set the title and description
		EditText eventTitle = (EditText) findViewById(R.id.event_edit_title);
		EditText description = (EditText) findViewById(R.id.event_edit_description);
		Spinner typeSpinner = (Spinner)findViewById(R.id.event_edit_type_spinner);
		
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                                                                             R.array.type_array,
                                                                             android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        
        if (savedInstanceState != null) {
        	Log.d("Event Edit", "End");
        	eventTitle.setText(savedInstanceState.getString("title"));
        	description.setText(savedInstanceState.getString("description"));
        	typeSpinner.setSelection(savedInstanceState.getInt("type"));
        	this.setTitle(savedInstanceState.getString("title"));
        	this.setDescription(savedInstanceState.getString("description"));
        	this.setType(Event.Type.typeIndices[savedInstanceState.getInt("type")]);
        } else {
		    eventTitle.setText(event.getTitle());
		    this.setTitle(event.getTitle());
		    description.setText(event.getDescription());
		    this.setDescription(event.getDescription());
		    //Set type;
		    int typeSelection = (event.getType().getValue() > 5) ? 0 : event.getType().getValue() - 1;
	        typeSpinner.setSelection(typeSelection);
	        this.setType(event.getType());
        }
		
        Log.d("Event Edit", "Title, and Description has been gotten");
		Log.d("Event Edit", "Type has been gotten");
        //Setting the Dates
        //Buttons
		String startString;
		String endString;
        if (savedInstanceState != null) {
        	Date tempStartDate = new Date(savedInstanceState.getLong("eventStartDate"));
        	Date tempEndDate = new Date(savedInstanceState.getLong("eventEndDate"));
        	startString = DateFormat.getTimeInstance(DateFormat.SHORT).format(tempStartDate);
            endString = DateFormat.getTimeInstance(DateFormat.SHORT).format(tempEndDate);
            this.setStartDate(tempStartDate);
            this.setEndDate(tempEndDate);
        } else {
        	startString = DateFormat.getTimeInstance(DateFormat.SHORT).format(event.getStartDate());
            endString = DateFormat.getTimeInstance(DateFormat.SHORT).format(event.getEndDate());
            this.setStartDate(event.getStartDate());
            this.setEndDate(event.getEndDate());
        }
        ((Button)findViewById(R.id.event_edit_start_button)).setText(startString);
        ((Button)findViewById(R.id.event_edit_end_button)).setText(endString);
        
        //Spinners
        Spinner startSpinner = (Spinner)findViewById(R.id.event_edit_start_spinner);
        Spinner endSpinner = (Spinner)findViewById(R.id.event_edit_end_spinner);
        ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
                                                                             R.array.relative_days,
                                                                             android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startSpinner.setAdapter(dateAdapter);
        endSpinner.setAdapter(dateAdapter);
        
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(this.getStartDate());
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(this.getEndDate());
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

        // Watch for changes to the spinners and text boxes.
        
        eventTitle.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				((CreateEventInterface)EventEdit.activity).setTitle(s.toString());
			}
			
			// Unused interface methods of TextWatcher.
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        description.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				((CreateEventInterface)EventEdit.activity).setDescription(s.toString());
			}
			
			// Unused interface methods of TextWatcher.
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	// Grab the enum by matching the indices of this spinner to the enumerated types.
            	int index = (position == Type.values().length - 1 ? 0 : position + 1);
            	((CreateEventInterface)EventEdit.activity).setType(Event.Type.typeIndices[index]);
            }
            // Interface requirements
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        
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

	public void onSaveInstanceState(Bundle savedInstanceState) {
    	if (event != null) {
    		savedInstanceState.putString("title", this.getEventTitle());
    		savedInstanceState.putString("description", this.getDescription());
    		savedInstanceState.putInt("type", this.getType().getValue());
    		savedInstanceState.putLong("eventStartDate", this.getStartDate().getTime());
    		savedInstanceState.putLong("eventEndDate", this.getEndDate().getTime());
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
			onBackPressed();
        	return true;
		} else if (item.getItemId() == R.id.event_edit_submit) {
			Event tempEvent = new Event(id, title, type, desc, startDate, endDate, location, attending);
			Log.d("EventEdit Submit", tempEvent.toJSON());
			updateEventAPICaller updateEvent = new updateEventAPICaller();
			updateEvent.execute(tempEvent);
			return true;
		} else if (item.getItemId() == R.id.event_edit_delete) {
			Event tempEvent = new Event(id, title, type, desc, startDate, endDate, location, attending);
			Log.d("EventEdit Delete", tempEvent.toJSON());
			deleteEventAPICaller deleteEvent = new deleteEventAPICaller();
			deleteEvent.execute(tempEvent);
			return true;
		} else if (item.getItemId() == R.id.action_back) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
	public void showEventTimeDialog(View v) {
    	DialogFragment timePicker;
    	switch (v.getId()) {
	    	case R.id.event_edit_start_button:
	            timePicker = new StartTimePicker();
	            timePicker.show(getSupportFragmentManager(), "startTimePicker");
	            break;
	    	case R.id.event_edit_end_button:
	    		timePicker = new EndTimePicker();
	    		timePicker.show(getSupportFragmentManager(), "endTimePicker");
	            break;
    	}
    }

	
	@SuppressLint("ValidFragment")
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
			return APICalls.updateEvent(event[0], Identity.getUserId(getApplicationContext()));
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

	private class deleteEventAPICaller extends AsyncTask<Event, Void, Boolean> {
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
		protected Boolean doInBackground(Event... event) {
			return APICalls.deleteEvent(event[0], Identity.getUserId(getApplicationContext()));
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Close the wizard and any progress indication.
			dialog.dismiss();
			dialog = null;
			if (result == false) {
				(Toast.makeText(getApplicationContext(), "Event already deleted, or could not delete event!", Toast.LENGTH_LONG)).show();
				EventEdit.this.finish();
				return;
			}
			
			// Pass the new event to the event viewer.
			//Intent intent = new Intent(EventEdit.this, EventViewer.class);
			//intent.putExtra("event", result);
		    //EventEdit.this.setResult(RESULT_OK, intent);
			EventEdit.this.finish();
		}
	}

}