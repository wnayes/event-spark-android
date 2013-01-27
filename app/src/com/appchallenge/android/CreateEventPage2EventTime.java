package com.appchallenge.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The second step of the create event wizard. From this fragment
 * a user can enter the time of the event and duration.
 */
public class CreateEventPage2EventTime extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_create_event_page_2;
        ViewGroup rootView = (ViewGroup)inflater.inflate(layoutId, container, false);
        
        // Populate the date spinner with the next two days.
        Spinner daySpinner = (Spinner)rootView.findViewById(R.id.daySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                                                             R.array.relative_days,
                                                                             android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);
        
        // Select the correct day spinner value.
        Date startDate = ((CreateEventInterface)getActivity()).getDate();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        if (c.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR))
        	daySpinner.setSelection(1);
        else
        	daySpinner.setSelection(0);

        // Set the time string.
        String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(startDate);
        ((TextView)rootView.findViewById(R.id.event_time_display)).setText(timeString);
        
        // Display the current duration.
        EditText durationBox = (EditText)rootView.findViewById(R.id.event_duration);
        Float duration = ((CreateEventInterface)getActivity()).getDuration();
        if (duration < 0.05 && duration > -0.05)
        	durationBox.setText("");
        else
        	durationBox.setText(duration.toString());
       
        // Watch for changes to the date spinner.
        daySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	Date startDate = ((CreateEventInterface)getActivity()).getDate();
            	Calendar c = Calendar.getInstance();
            	c.setTime(startDate);
            	Calendar today = Calendar.getInstance();
                today.setTime(new Date());
            	if (c.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
            		c.add(Calendar.DATE, 1);
            	else
            		c.add(Calendar.DATE, -1);
            	((CreateEventInterface)getActivity()).setDate(c.getTime());
            }
            // Interface requirements
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        
       // Watch for changes to the event duration input.
        durationBox.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (s.toString().equals(""))
					((CreateEventInterface)getActivity()).setDuration(0);
				else
				  ((CreateEventInterface)getActivity()).setDuration(Float.parseFloat(s.toString()));
			}
			
			// Unused interface methods of TextWatcher.
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        return rootView;
    }
}
