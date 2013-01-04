package com.appchallenge.android;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Window;

/**
 * Displays a user's location and surrounding events.
 */
public class EventViewer extends SherlockFragmentActivity implements LocationListener, LocationSource {
    // Note that this may be null if the Google Play services APK is not available.
    private GoogleMap mMap;

    private OnLocationChangedListener mListener;
    private LatLng currentLocation = new LatLng(0,0);
    private float currentZoom = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        setUpMapIfNeeded();

        // Grab the latest location information.
        LocationFinder locationFinder = new LocationFinder();
        locationFinder.getLocation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_event_viewer, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.menu_create_event:
            	// Launch the wizard for creating a new event.
            	Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);
            	startActivity(createEvent);
            	return true;
	        case R.id.menu_refresh_events:
	        	// Refresh the event listing.
	            //LocationFinder locationFinder = new LocationFinder();
	            //locationFinder.getLocation(this);
	        	
	        	// Call the API for new event markers.
	        	new EventAPICaller().execute();
	        	return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            
            // Register the LocationSource
            mMap.setLocationSource(this);
            
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    // This is where we can add markers or lines, add listeners or move the camera.
    // This should only be called once and when we are sure that the map is not null.
    private void setUpMap() {
    	//mMap.getUiSettings().setZoomControlsEnabled(false);
    	mMap.setMyLocationEnabled(true);
    	mMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom));
    	
    }

	@Override
	public void onLocationChanged(Location location) {
		setProgressBarVisibility(false);

		// Feed the map listener the location to update the location indicator.
		if (mListener != null)
	        mListener.onLocationChanged(location);

		// Act based on the new location.
        if (location != null) {
        	currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
	        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
        }

//		// Keep the user's zoom if they have changed it.
//		if (Math.abs(currentZoom - mMap.getCameraPosition().zoom) > 0.2)
//			currentZoom = mMap.getCameraPosition().zoom;
	}

	// Methods required (besides onLocationChanged()) for LocationListener.
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	// Returns the distance in km between two LatLng objects.
	// Maybe needed sooner or later.
	private double getDistanceBetweenLatLngs(LatLng location1, LatLng location2) {
		double latitudeDif = Math.toRadians(location2.latitude - location1.latitude);
		double longitudeDif = Math.toRadians(location2.longitude - location1.longitude);
		
		// Perform Haversine formula.
		double a = Math.sin(latitudeDif / 2) * Math.sin(latitudeDif / 2)
				 + Math.cos(Math.toRadians(location1.latitude)) * Math.cos(Math.toRadians(location2.latitude))
				 + Math.sin(longitudeDif / 2) * Math.sin(longitudeDif / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return 6371.0 * c;
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
	}

	@Override
	public void deactivate() {
		mListener = null;
	}
	
	/**
	 * Performs an asynchronous API call to find nearby events.
	 */
	private class EventAPICaller extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// Set up some progress indication?
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			//return APICalls.getEventsNearLocation( ... );
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// Remove progress UI.
		}
		
	}
}
