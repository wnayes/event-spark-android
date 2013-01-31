package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
        Spinner startSpinner = (Spinner)rootView.findViewById(R.id.start_spinner);
        Spinner endSpinner = (Spinner)rootView.findViewById(R.id.end_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                                                             R.array.relative_days,
                                                                             android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startSpinner.setAdapter(adapter);
        endSpinner.setAdapter(adapter);
        
        Date startDate = ((CreateEventInterface)getActivity()).getDate();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        Calendar today = Calendar.getInstance();
        Date endDate = ((CreateEventInterface)getActivity()).getEndDate();
        
     // Set the time string.
        String startString = DateFormat.getTimeInstance(DateFormat.SHORT).format(startDate);
        Button start_button = ((Button) rootView.findViewById(R.id.event_start_button));
        start_button.setText(startString);
       
        Calendar ce = Calendar.getInstance();
        ce.setTime(endDate);
        //ce.add(Calendar.HOUR, 3);
        String endString = DateFormat.getTimeInstance(DateFormat.SHORT).format(ce.getTime());
        Button end_button = ((Button) rootView.findViewById(R.id.event_end_button));
        end_button.setText(endString);
        
        // Select the correct day spinner value.
        
        today.setTime(new Date());
        if (c.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR)) {
        	startSpinner.setSelection(1);
            endSpinner.setSelection(1);
        } else {
        	startSpinner.setSelection(0);
            endSpinner.setSelection(0);
        }
        // Watch for changes to the date spinner.
        startSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
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
        
        endSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
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
            	((CreateEventInterface)getActivity()).setEndDate(c.getTime());
            }
            // Interface requirements
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        
        return rootView;
    }
}
