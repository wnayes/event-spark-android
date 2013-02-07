package com.appchallenge.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EventDetailsAttendanceTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View attendanceView = inflater.inflate(R.layout.fragment_event_details_attendance, container, false);
    	
    	// Receive and display attendance count from parent activity.
    	String attendance = ((Integer)listener.getEventAttendance()).toString();
    	((TextView)attendanceView.findViewById(R.id.event_attendance_count)).setText(attendance);

    	return attendanceView;
    }

    /**
	 * Interface allowing the attendance tab fragment to receive Event
     * information from the parent activity.
	 */
	public interface AttendanceTabListener {
		public int getEventAttendance();
	}
	private AttendanceTabListener listener;
	
	/**
	 * Attach the parent activity as a listener to receive Event information.
	 */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AttendanceTabListener)
            listener = (AttendanceTabListener)activity;
        else
            throw new ClassCastException(activity.toString() + " must implemenet AttendanceTabListener");
    }
	public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
