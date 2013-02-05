package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
        
        Date startDate = ((CreateEventInterface)getActivity()).getStartDate();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Date endDate = ((CreateEventInterface)getActivity()).getEndDate();
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        
        Calendar today = Calendar.getInstance();
        
        // Set the time strings on the display buttons.
        String startString = DateFormat.getTimeInstance(DateFormat.SHORT).format(startDate);
        ((Button)rootView.findViewById(R.id.event_start_button)).setText(startString);

        String endString = DateFormat.getTimeInstance(DateFormat.SHORT).format(endDate);
        ((Button)rootView.findViewById(R.id.event_end_button)).setText(endString);
        
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
            	Date startDate = ((CreateEventInterface)getActivity()).getStartDate();
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
	
            	((CreateEventInterface)getActivity()).setStartDate(startCalendar.getTime());
            }
            // Interface requirements
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        endSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	Date endDate = ((CreateEventInterface)getActivity()).getEndDate();
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

            	((CreateEventInterface)getActivity()).setEndDate(endCalendar.getTime());
            }
            // Interface requirements
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        
        return rootView;
    }
}
