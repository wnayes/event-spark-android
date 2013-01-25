package com.appchallenge.android;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

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
        
        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup1);        
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            	int button = group.getCheckedRadioButtonId();

            	switch (button) {
	                case R.id.radioButton_no:
	                    //CreateEvent.switcher.setDisplayedChild(0);
	                    break;
	                case R.id.radioButton_yes:
	                    //CreateEvent.switcher.setDisplayedChild(1);
	                    break;
                }
            }
        });

        Spinner spinner = ((Spinner)rootView.findViewById(R.id.spinner1));
        Context context = getActivity().getApplicationContext();
        Calendar cal = Calendar.getInstance();
    	List<String> date;
        
    	date = new ArrayList<String>();
    	CreateEvent.cal_1 = cal;
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day1 = cal.get(Calendar.DAY_OF_MONTH);
        date.add(numberDateToString(month)+" "+Integer.toString(day1)+", "+Integer.toString(year));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        CreateEvent.cal_2 = cal;
        int year_1 = cal.get(Calendar.YEAR);
        int month_1 = cal.get(Calendar.MONTH);
        int day1_1 = cal.get(Calendar.DAY_OF_MONTH);
        date.add(numberDateToString(month_1)+" "+Integer.toString(day1_1)+", "+Integer.toString(year_1));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        CreateEvent.cal_3 = cal;
        int year_2 = cal.get(Calendar.YEAR);
        int month_2 = cal.get(Calendar.MONTH);
        int day1_2 = cal.get(Calendar.DAY_OF_MONTH);
        date.add(numberDateToString(month_2)+" "+Integer.toString(day1_2)+", "+Integer.toString(year_2));
        
        
        //spinner;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,date);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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
    
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        //Context context = getApplicationContext();
        //CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;
        //Toast toast = Toast.makeText(context, date, duration);
        //toast.show();
        switch(view.getId()) {
            case R.id.radioButton_no:
                if (checked){
                	CreateEvent.switcher.setDisplayedChild(0);
                }
                break;
            case R.id.radioButton_yes:
                if (checked){
                    CreateEvent.switcher.setDisplayedChild(1);
                }
                break;
    
        }
    }
    private String numberDateToString(int date){
    	String month = "";
    	switch(date){
    	case 0: month = "January";
    	        break;
    	case 1: month = "February";
    	        break;
    	case 2: month = "March";
    	        break;
    	case 3: month = "April";
    	        break;
    	case 4: month = "May";
    	        break;
    	case 5: month = "June";
    	        break;
    	case 6: month = "July";
    	        break;
    	case 7: month = "August";
    	        break;
    	case 8: month = "September";
    	        break;
    	case 9: month = "October";
    	        break;
    	case 10: month = "November";
    	        break;
    	case 11: month = "December";
    			break;
    	}
    	
    	return month;
    }
}
