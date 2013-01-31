package com.appchallenge.android;

import java.util.HashMap;
import java.util.Timer;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.appchallenge.android.LocationFinder.GetLastLocation;

/**
 * Displays a user's location and surrounding events.
 */
public class EventViewer extends SherlockFragmentActivity implements LocationListener, LocationSource, OnInfoWindowClickListener {
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
     */
    private LatLng currentLocation;

    /**
     * Values saving the current location and zoom of the Map.
     * Currently defaulted to Minneapolis, MN.
     */
    private LatLng currentMapLocation = new LatLng(44.9764164, -93.2323474);
    private float currentMapZoom = 12;

    /**
     * Array of Events we have downloaded for the user.
     */
    private Event[] currentEvents;
    
    /**
     * Maps the existing markers to their corresponding Event ID.
     * This allows actions taken on a marker to be traced back to the
     * target Event.
     */
    HashMap<Marker, Integer> eventMarkerMap = new HashMap<Marker, Integer>();

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
                for (int i = 0; i < JSONEvents.length; ++i)
            	    this.currentEvents[i] = new Event(JSONEvents[i]);
                reloadEventMarkers();
            }
            
            // Restore any closed dialogs.
            if (savedInstanceState.getBoolean("internetDialogOpen", false))
            	this.displayConnectivityDialog();
            if (savedInstanceState.getBoolean("noLocationSourceDialogOpen", false))
            	this.showNoLocationSourceDialog();
        }

        setUpMapIfNeeded();
        
        if (savedInstanceState != null) {
        	// Feed the map listener it's previous to restore the location indicator.
        	if (savedInstanceState.containsKey("currentLatitude") &&
                savedInstanceState.containsKey("currentLongitude")) {
        		Location oldLoc = new Location("savedState");
	            oldLoc.setLatitude(savedInstanceState.getDouble("currentLatitude"));
	            oldLoc.setLongitude(savedInstanceState.getDouble("currentLongitude"));
	            oldLoc.setAccuracy(savedInstanceState.getFloat("currentAccuracy", 0));

	            currentLocation = new LatLng(oldLoc.getLatitude(), oldLoc.getLongitude());

                if (mListener != null)
	                mListener.onLocationChanged(oldLoc);
        	}
        }

        // Grab the latest location information.
        if (savedInstanceState == null)
        	this.updateUserLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    protected void onStop() {
    	// Prevent dialogs from leaking and remaining open.
    	if (internetDialog != null && internetDialog.isShowing()) {
    		internetDialog.cancel();
    		internetDialog = null;
    	}
    	if (noLocationSourceDialog != null && noLocationSourceDialog.isShowing()) {
    		noLocationSourceDialog.cancel();
    		noLocationSourceDialog = null;
    	}
        super.onStop();
    }

    private Menu _menu;
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_event_viewer, menu);
        
        // Keep a reference to the menu for later uses (refresh indicator change).
        this._menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == R.id.menu_create_event) {
			Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);

			// Pass the current location to the wizard so the maps appear synced.
			if (currentLocation != null) {
                createEvent.putExtra("latitude", currentLocation.latitude);
			    createEvent.putExtra("longitude", currentLocation.longitude);
			}

			// Launch the wizard for creating a new event.
			startActivity(createEvent);
			return true;
		} else if (item.getItemId() == R.id.menu_refresh_events) {
			// Refresh the event listing.
			if (!isOnline()) {
				// Tell the user to connect to the Internet.
				this.displayConnectivityDialog();
			}
			else if (currentLocation != null) {
			    new getEventsNearLocationAPICaller().execute(currentLocation);
			}
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
    	    savedInstanceState.putFloat("currentAccuracy", mMap.getMyLocation().getAccuracy());
    	}

    	// Our Events cannot be directly put into the Bundle, but
    	// they can be stringified back into JSON first.
    	if (this.currentEvents != null) {
            String[] JSONEvents = new String[this.currentEvents.length];
            for (int i = 0; i < this.currentEvents.length; ++i) {
    	    	JSONEvents[i] = this.currentEvents[i].toJSON();
            }
            savedInstanceState.putStringArray("currentEvents", JSONEvents);
    	}
    	
    	// Dialogs get thrown under the bus on configuration changes, so their
        // state must be remembered.
    	if (internetDialog != null && internetDialog.isShowing())
    		savedInstanceState.putBoolean("internetDialogOpen", true);
    	if (noLocationSourceDialog != null && noLocationSourceDialog.isShowing())
    		savedInstanceState.putBoolean("noLocationSourceDialogOpen", true);

        super.onSaveInstanceState(savedInstanceState);
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
            if (mMap != null)
                setUpMap();
        }
    }

    /**
     *  This is where we can add markers or lines, add listeners or move the camera.
     *  This should only be called once and when we are sure that the map is not null.
     */
    private void setUpMap() {
    	//mMap.getUiSettings().setZoomControlsEnabled(false);
    	mMap.setMyLocationEnabled(true);
    	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMapLocation, currentMapZoom));
    	mMap.setOnInfoWindowClickListener(this);
    }

    /**
     * Occurs when the user clicks the marker info windows.
     * Shows the extended information activity for the marker.
     * @param marker
     */
	@Override
	public void onInfoWindowClick(Marker marker) {
		// Retrieve the marker event via hash map.

		// It seems like it might be possible to map directly from marker to
		// Event objects, but there were some reference issues that made
		// mapping to ID more appealing even despite the increased complexity.
		int selectedId = eventMarkerMap.get(marker);
		Event selectedEvent = null;
		for (Event event : this.currentEvents) {
			if (event.getId() == selectedId)
				selectedEvent = event;
		}
		
		if (selectedEvent == null) {
			Log.e("EventViewer.onInfoWindowClick", "selectedEvent == null");
			return;
		}
		
		// Pass information about the event to the details activity.
		Intent eventDetails = new Intent(EventViewer.this, EventDetails.class);
		eventDetails.putExtra("id", selectedEvent.getId());
		eventDetails.putExtra("title", selectedEvent.getTitle());
		eventDetails.putExtra("description", selectedEvent.getDescription());
		eventDetails.putExtra("startDate", selectedEvent.getStartTime());
		eventDetails.putExtra("endDate", selectedEvent.getEndTime());
		eventDetails.putExtra("latitude", selectedEvent.getLocation().latitude);
		eventDetails.putExtra("longitude", selectedEvent.getLocation().longitude);
		startActivity(eventDetails);
	}

    /**
     * Adds the current Events tracked in this activity to the map.
     */
    private void reloadEventMarkers() {
    	if (this.currentEvents == null || this.currentEvents.length == 0)
    		return;

    	setUpMapIfNeeded();
    	mMap.clear();
        eventMarkerMap.clear();
        for (Event event : this.currentEvents) {
    		Marker m = mMap.addMarker(event.toMarker());
    		eventMarkerMap.put(m, event.getId());
    	}
    }
    
    private AlertDialog noLocationSourceDialog;
    private void showNoLocationSourceDialog() {
    	if (noLocationSourceDialog != null) {
    		noLocationSourceDialog.cancel();
		}
    	noLocationSourceDialog = new AlertDialog.Builder(this).create();
    	noLocationSourceDialog.setTitle("Location Source Needed");
    	noLocationSourceDialog.setMessage("Turn on GPS or your cellular connection and try again!");
    	noLocationSourceDialog.setIcon(android.R.drawable.ic_dialog_alert);
    	noLocationSourceDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
	        }
	    });
    	noLocationSourceDialog.show();
    }
    public void updateUserLocation() {
        LocationFinder locationFinder = new LocationFinder();
        boolean sourcesExist = locationFinder.getLocation(this);
        if (!sourcesExist) {
        	// Show a dialog indicating to turn on some location source.
        	showNoLocationSourceDialog();
        }
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
        	if (isOnline())
        	    new getEventsNearLocationAPICaller().execute(currentLocation);
        }
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
	 * Determines if the device has internet connectivity.
	 * @return Whether a data connection is available.
	 */
	public boolean isOnline() {
        ConnectivityManager connectivityManager =
          (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable());
    }

	/**
	 * Shows a dialog informing the user that an internet connection is not available.
	 */
	private AlertDialog internetDialog;
	public void displayConnectivityDialog() {
		if (internetDialog != null) {
			internetDialog.cancel();
		}
		internetDialog = new AlertDialog.Builder(this).create();
		internetDialog.setTitle("Internet Connection Needed");
		internetDialog.setMessage("Connect your device to an Internet source and try again!");
		internetDialog.setIcon(android.R.drawable.ic_dialog_alert);
		internetDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
	        }
	    });
		internetDialog.show();
	}

	/**
	 * Performs an asynchronous API call to find nearby events.
	 */
	private class getEventsNearLocationAPICaller extends AsyncTask<LatLng, Void, Event[]> {
		/**
		 * Quick access to the refresh button in the actionbar.
		 */
		MenuItem refreshItem;

		@Override
		protected void onPreExecute() {
			// Establish progress UI changes.
		    refreshItem = _menu.findItem(R.id.menu_refresh_events);
			refreshItem.setActionView(R.layout.actionbar_refresh_progress);
		}

		@Override
		protected Event[] doInBackground(LatLng... location) {
			return APICalls.getEventsNearLocation(location[0]);
		}

		@Override
		protected void onPostExecute(Event[] result) {
			// Remove progress UI.
			refreshItem.setActionView(null);
			refreshItem = null;

			// Keep track of these events and populate the map.
			if (result == null) {
				Toast.makeText(getApplicationContext(), "No events found near you!", Toast.LENGTH_LONG)
				     .show();
				currentEvents = null;
				eventMarkerMap.clear();
				return;
			}

			currentEvents = result.clone();
			reloadEventMarkers();
		}
	}
}
