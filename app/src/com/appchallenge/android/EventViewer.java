package com.appchallenge.android;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

/**
 * Displays a user's location and surrounding events.
 */
public class EventViewer extends SherlockFragmentActivity implements LocationListener, LocationSource {
    /**
     * Object representing the Google Map display of our Events.
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

    /**
     * Listener for the Google Map user location.
     */
    private OnLocationChangedListener mListener;
    
    /**
     * Store the current location and zoom of the user.
     * Currently defaulted to Minneapolis, MN.
     */
    private LatLng currentLocation;

    /**
     * Values saving the current location and zoom of the Map.
     */
    private LatLng currentMapLocation = new LatLng(44.9764164, -93.2323474);
    private float currentMapZoom = 12;
    
    /**
     * Array of Events we have downloaded for the user.
     */
    private Event[] currentEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);
        
        // We may be reloading due to a configuration change.
        // Load any available information from the previous instance.
        if (savedInstanceState != null) {
            currentMapLocation = new LatLng(savedInstanceState.getDouble("currentMapLatitude"),
                                            savedInstanceState.getDouble("currentMapLongitude"));
            currentMapZoom = savedInstanceState.getFloat("currentMapZoom");

            String[] JSONEvents = savedInstanceState.getStringArray("currentEvents");
            if (JSONEvents != null) {
            	this.currentEvents = new Event[JSONEvents.length];
                for (int i = 0; i < JSONEvents.length; ++i) {
            	    this.currentEvents[i] = new Event(JSONEvents[i]);
                }
            }
        }

        setUpMapIfNeeded();
        
        if (savedInstanceState != null) {
        	// Feed the map listener it's previous to restore the location indicator.
        	if (savedInstanceState.containsKey("currentLatitude") &&
                savedInstanceState.containsKey("currentLongitude")) {
        		Location oldLoc = new Location("savedState");
	            oldLoc.setLatitude(savedInstanceState.getDouble("currentLatitude"));
	            oldLoc.setLongitude(savedInstanceState.getDouble("currentLongitude"));

	            currentLocation = new LatLng(oldLoc.getLatitude(), oldLoc.getLongitude());

                if (mListener != null)
	                mListener.onLocationChanged(oldLoc);
        	}
        }

        // Grab the latest location information.
        if (savedInstanceState == null) {
            LocationFinder locationFinder = new LocationFinder();
            locationFinder.getLocation(this);
        }
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
        if (item.getItemId() == R.id.menu_create_event) {
			// Launch the wizard for creating a new event.
			Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);
			startActivity(createEvent);
			return true;
		} else if (item.getItemId() == R.id.menu_refresh_events) {
			// Refresh the event listing.
			new getEventsNearLocationAPICaller().execute();
			return true;
		}

        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Android can arbitrarily reload our Activity, especially during screen rotations,
     * keyboard opening/closing, etc. When this happens, the full onCreate, onResume, etc.
     * sequence takes place. To avoid losing data (our Events in particular), the basic
     * way to preserve state is to override this function and store primitive types in
     * the savedInstanceState Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putDouble("currentMapLatitude", mMap.getCameraPosition().target.latitude);
    	savedInstanceState.putDouble("currentMapLongitude",  mMap.getCameraPosition().target.longitude);
    	savedInstanceState.putFloat("currentMapZoom", mMap.getCameraPosition().zoom);
    	if (currentLocation != null) {
    	    savedInstanceState.putDouble("currentLatitude", currentLocation.latitude);
    	    savedInstanceState.putDouble("currentLongitude", currentLocation.longitude);
    	}
    	// Our Events cannot be directly put into the Bundle, but
    	// they can be stringified back into JSON first.
    	if (this.currentEvents == null)
            return;

        String[] JSONEvents = new String[this.currentEvents.length];
        for (int i = 0; i < this.currentEvents.length; ++i) {
    		JSONEvents[i] = this.currentEvents[i].toJSON();
        }
        savedInstanceState.putStringArray("currentEvents", JSONEvents);
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
    	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMapLocation, currentMapZoom));    	
    }

	@Override
	public void onLocationChanged(Location location) {
		// Feed the map listener the location to update the location indicator.
		if (mListener != null && location != null)
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
	private class getEventsNearLocationAPICaller extends AsyncTask<Void, Void, Void> {

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
