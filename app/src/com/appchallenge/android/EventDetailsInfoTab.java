package com.appchallenge.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EventDetailsInfoTab extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View infoView = inflater.inflate(R.layout.fragment_event_details_info, container, false);

	    // Display the Event title and description.
	    ((TextView)infoView.findViewById(R.id.event_details_title)).setText(listener.getEventTitle());
	    String description = listener.getEventDescription();
	    TextView descBox = (TextView)infoView.findViewById(R.id.event_details_description);
	    if (description.isEmpty()) {
	    	descBox.setText(R.string.event_description_empty);
	    	descBox.setTypeface(null, Typeface.ITALIC);
	    }
	    else {
	    	descBox.setText(description);
	    	descBox.setTypeface(null, Typeface.NORMAL);
	    }
	    
	    // Display different date strings based on the time of the event.
	    Calendar today = Calendar.getInstance();
	    Calendar startCalendar = Calendar.getInstance();
	    startCalendar.setTime(listener.getEventStartDate());
	    Calendar endCalendar = Calendar.getInstance();
	    endCalendar.setTime(listener.getEventEndDate());

	    String dateString = "";
	    if (endCalendar.before(today)) {
	    	dateString += "This event has ended.";
	    	((TextView)infoView.findViewById(R.id.event_details_date_description)).setText(dateString);
		    return infoView;
	    }

	    if (startCalendar.after(today)) {
	    	if (startCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
	    		dateString += "Begins today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + ". ";
	    	else
	    		dateString += "Begins tomorrow at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(startCalendar.getTime()) + ". ";
	    }

	    if (endCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
    		dateString += "Ends today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + ".";
    	else
    		dateString += "Ends tomorrow at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(endCalendar.getTime()) + ".";
	    
	    ((TextView)infoView.findViewById(R.id.event_details_date_description)).setText(dateString);

	    return infoView;
	}

	/**
	 * Interface allowing the info tab fragment to receive Event information from
	 * the parent activity.
	 */
	public interface InfoTabListener {
		public String getEventTitle();
		public String getEventDescription();
		public Date getEventStartDate();
		public Date getEventEndDate();
	}
	private InfoTabListener listener;

	/**
	 * Attach the parent activity as a listener to receive Event information.
	 */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof InfoTabListener)
            listener = (InfoTabListener)activity;
        else
            throw new ClassCastException(activity.toString() + " must implemenet InfoTabListener");
    }
	public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
