package com.appchallenge.android;

import android.os.Bundle;

//import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The third step of the create event wizard. This fragment holds a map
 * that the user can modify the event location marker position.
 */
public class CreateEventPage3EventLocation extends SupportMapFragment {
	// The marker representing the new event.
	//private MarkerOptions eventMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	String info = String.format("Move the marker to your location");
    	View view = super.onCreateView(inflater, container, savedInstanceState);
    	LatLng location = ((CreateEventInterface)getActivity()).getLocation();
    	getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    	final Marker marker = getMap().addMarker(new MarkerOptions()
    	                       .position(location)
    	                       .title(((CreateEventInterface)getActivity()).getEventTitle())
    	                       .snippet(info)
    	                       .draggable(true));
    	getMap().setOnMarkerDragListener(new OnMarkerDragListener(){
    					
			@Override
			public void onMarkerDragEnd(Marker arg0) {
				LatLng new_location = marker.getPosition();
	    		((CreateEventInterface)getActivity()).setLocation(new_location);
			}
			
			//Unused Functions
			@Override
			public void onMarkerDragStart(Marker arg0){}			
			@Override
			public void onMarkerDrag(Marker arg0){}
    	});
    	marker.showInfoWindow();
    	
    	
        return view;
    }


}
