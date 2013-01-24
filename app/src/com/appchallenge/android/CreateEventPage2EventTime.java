package com.appchallenge.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;



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
        
        // Set the UI start date controls to the current start date (defaults to now).
        Calendar cal = Calendar.getInstance();
        cal.setTime(((CreateEventInterface)getActivity()).getDate());
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        ((TextView)rootView.findViewById(R.id.event_time_display))
                           .setText(formatter.format(cal.getTime()));
        formatter = new SimpleDateFormat("MM/dd/yyyy");
        ((TextView)rootView.findViewById(R.id.event_date_display))
                           .setText(formatter.format(cal.getTime()));
        
        // TODO: Handle changing the UI when checkbox changed occurs.
//        CheckBox curHappeningCheckbox = (CheckBox)rootView.findViewById(R.id.checkbox_currently_happening);
//        curHappeningCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Toast.makeText(getActivity().getApplicationContext(),
//                               "Checked: " + ((Boolean)isChecked).toString(),
//                               Toast.LENGTH_SHORT).show();
//            }
//        });

        return rootView;
    }
}
