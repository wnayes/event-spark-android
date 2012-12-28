package com.appchallenge.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return rootView;
    }
}
