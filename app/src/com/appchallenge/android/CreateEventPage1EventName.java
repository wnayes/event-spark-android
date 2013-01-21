package com.appchallenge.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
        
        return rootView;
    }
}
