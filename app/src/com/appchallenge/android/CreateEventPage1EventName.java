package com.appchallenge.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * The first step of the create event wizard. This collects the name of
 * the event from the user.
 */
public class CreateEventPage1EventName extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_create_event_page_1;
        ViewGroup rootView = (ViewGroup)inflater.inflate(layoutId, container, false);
        
        EditText titleBox = (EditText)rootView.findViewById(R.id.event_name);
        EditText descBox = (EditText)rootView.findViewById(R.id.event_description);
        
        // Read the title and description from the parent Activity.
        titleBox.setText(((CreateEventInterface)getActivity()).getEventTitle());
        descBox.setText(((CreateEventInterface)getActivity()).getDescription());

        // Handle keeping track of future updates to the text.
        titleBox.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				((CreateEventInterface)getActivity()).setTitle(s.toString());
			}
			
			// Unused interface methods of TextWatcher.
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        descBox.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				((CreateEventInterface)getActivity()).setDescription(s.toString());
			}
			
			// Unused interface methods of TextWatcher.
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        return rootView;
    }
}
