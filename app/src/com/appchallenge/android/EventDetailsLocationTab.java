package com.appchallenge.android;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EventDetailsLocationTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View locationView = inflater.inflate(R.layout.fragment_event_details_location, container, false);
        
        // Define the "Get Directions" button to send an intent to another app.
        Button button = (Button)locationView.findViewById(R.id.get_directions_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Prepare maps url query url parameters.
            	LatLng startLoc = listener.getUserLocation();
            	LatLng endLoc = listener.getEventLocation();
            	String startCoords = ((Double)startLoc.latitude).toString() + "," + ((Double)startLoc.longitude).toString();
            	String endCoords = ((Double)endLoc.latitude).toString() + "," + ((Double)endLoc.longitude).toString();
            	String url = "http://maps.google.com/maps?saddr=" + startCoords + "&daddr=" + endCoords;
            	Log.d("EventDetailsLocationTab", "Get directions, " + url);

                // Pass an intent to an activity that can provide directions.
            	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        return locationView;
    }
    
    /**
	 * Interface allowing the location tab fragment to receive Event information from
	 * the parent activity.
	 */
	public interface LocationTabListener {
		public LatLng getEventLocation();
		public LatLng getUserLocation();
	}
	private LocationTabListener listener;

	/**
	 * Attach the parent activity as a listener to receive Event information.
	 */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof LocationTabListener)
            listener = (LocationTabListener)activity;
        else
            throw new ClassCastException(activity.toString() + " must implemenet LocationTabListener");
    }
	public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
