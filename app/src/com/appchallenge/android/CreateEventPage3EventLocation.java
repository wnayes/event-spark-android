package com.appchallenge.android;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The third step of the create event wizard. This fragment holds a map
 * that the user can modify the event location marker position.
 */
public class CreateEventPage3EventLocation extends SupportMapFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = super.onCreateView(inflater, container, savedInstanceState);
    	LatLng location = ((CreateEventInterface)getActivity()).getLocation();
    	getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    	
        MarkerOptions m = ((CreateEventInterface)getActivity()).getMarker();
        m.position(location);
        m.title(((CreateEventInterface)getActivity()).getEventTitle());
        m.snippet("Move the marker to the event.");

        final Marker marker = getMap().addMarker(m);

        // Set a listener to update the marker location when dragged by the user.
    	getMap().setOnMarkerDragListener(new OnMarkerDragListener() {		
			@Override
			public void onMarkerDragEnd(Marker arg0) {
	    		((CreateEventInterface)getActivity()).setLocation(marker.getPosition());

	    		// Have the map follow the marker and center on it.
	    		getMap().animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);
			}
			
			// Unused interface methods
			public void onMarkerDragStart(Marker arg0) {}
			public void onMarkerDrag(Marker arg0) {}
    	});

    	marker.showInfoWindow();
        return view;
    }
}
