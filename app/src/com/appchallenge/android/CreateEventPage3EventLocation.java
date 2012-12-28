package com.appchallenge.android;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The third step of the create event wizard. This fragment holds a map
 * that the user can find their precise location with.
 */
public class CreateEventPage3EventLocation extends SupportMapFragment {
	// The marker representing the new event.
	private MarkerOptions eventMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	View view = super.onCreateView(inflater, container, savedInstanceState);  	
        return view;
    }
}
