package com.appchallenge.android;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.conn.ConnectTimeoutException;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.appchallenge.android.Event.Type;
import com.appchallenge.android.TypeFilterDialogFragment.TypeFilterDialogListener;

/**
 * Displays a user's location and surrounding events.
 */
public class EventViewer extends SherlockFragmentActivity implements LocationListener,
                                                                     LocationSource,
                                                                     OnInfoWindowClickListener,
                                                                     TypeFilterDialogListener {
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
    private float currentMapZoom = 15;

    /**
     * Indicates whether receiving the user's location should move the map.
     * For instance, the viewer may go to a location such that certain events
     * will be visible.
     */
    private boolean atSpecifiedLocation = false;

    /**
     * A set of bounds we wish to show in the viewer, set due to receiving
     * a list of locations from a notification.
     */
    private LatLngBounds mapBounds;

    /**
     * Array of Events we have downloaded for the user.
     */
    private ArrayList<Event> currentEvents = new ArrayList<Event>();
    
    /**
     * Maps the existing markers to their corresponding Event ID.
     * This allows actions taken on a marker to be traced back to the
     * target Event.
     */
    HashMap<Marker, Integer> eventMarkerMap = new HashMap<Marker, Integer>();
    
    /**
     * Collection of event Types used for filtering events. The user
     * can choose to remove these using the TypeFilterDialogFragment from the
     * action bar.
     */
    ArrayList<Type> filterTypes = new ArrayList<Type>();
    
    /**
     * Provides access to our local sqlite database.
     */
    private LocalDatabase localDB;

    /** Keeps track of the help view state, used to keep it open / closed as necessary. */
    private Boolean helpOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d("EventViewer.onCreate", "(Re)creating EventViewer.java");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // Restore the state of the initial screen.
        if (savedInstanceState != null) {
        	findViewById(R.id.initialScreen).setVisibility(savedInstanceState.getInt("initialScreenVisible", View.GONE));
        	findViewById(R.id.initialNoSources).setVisibility(savedInstanceState.getInt("initialNoSourcesVisible", View.GONE));
        	findViewById(R.id.createEventButton).setVisibility(savedInstanceState.getInt("initialCreateEventVisible", View.GONE));
        	findViewById(R.id.initialMainActions).setVisibility(savedInstanceState.getInt("initialViewEventsVisible", View.GONE));
        	findViewById(R.id.initialProgressLayout).setVisibility(savedInstanceState.getInt("initialLocProgressVisible", View.GONE));
        }

        // Restore the help dialog if the user has not yet acknowledged it.
        SharedPreferences helpPrefs = getSharedPreferences(Settings.HELP_FILE, 0);
        this.helpOpen = savedInstanceState != null ? savedInstanceState.getBoolean("helpOpen", false) : false;
        if (!helpPrefs.getBoolean(Settings.HELP_VIEWER_SEEN, false) || this.helpOpen) {
        	this.helpOpen = true;
        	findViewById(R.id.help_viewer).setVisibility(View.VISIBLE);
        }
        else
        	findViewById(R.id.help_viewer).setVisibility(View.GONE);

        // Instantiate the list of visible types.
        if (filterTypes.size() == 0) {
        	for (Type type : Type.typeIndices)
        	    filterTypes.add(type);
        }

        // We may be reloading due to a configuration change.
        // Load any available information from the previous instance.
        if (savedInstanceState != null) {
            currentMapLocation = savedInstanceState.getParcelable("currentMapLocation");
            currentMapZoom = savedInstanceState.getFloat("currentMapZoom");

            // Restore the Event collection and markers.
            this.currentEvents = savedInstanceState.getParcelableArrayList("currentEvents");
            reloadEventMarkers();

            // Restore any closed dialogs.
            if (savedInstanceState.getBoolean("noLocationSourceDialogOpen", false))
            	this.showNoLocationSourceDialog();
        }

        // If we receive a "newEvents" key in the bundle, it means this activity has
        // been spawned for the purpose of viewing some events from a notification.
        if (getIntent().hasExtra("newEvents") && savedInstanceState == null) {
        	// The initial screen should be hidden.
        	this.hideInitialScreen();

        	// The map will be zoomed such that we can see all of these events.
        	LatLngBounds.Builder builder = LatLngBounds.builder();
        	ArrayList<Event> newEvents = getIntent().getExtras().getParcelableArrayList("newEvents");
        	for (Event e : newEvents)
        		builder.include(e.getLocation());
        	this.mapBounds = builder.build();
        	this.atSpecifiedLocation = true;
        }

        setUpMapIfNeeded();

        if (savedInstanceState != null) {
        	// Feed the map listener it's previous to restore the location indicator.
        	if (savedInstanceState.containsKey("currentUserLocation")) {
        		Location oldLoc = new Location("savedState");
        		this.currentLocation = savedInstanceState.getParcelable("currentUserLocation");
	            oldLoc.setLatitude(this.currentLocation.latitude);
	            oldLoc.setLongitude(this.currentLocation.longitude);
	            oldLoc.setAccuracy(savedInstanceState.getFloat("currentUserLocationAccuracy", 0));

                if (mListener != null)
	                mListener.onLocationChanged(oldLoc);
        	}
        }

        // Grab the latest location information.
        if (savedInstanceState == null) {
        	this.updateUserLocation();
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

    	// Close our database helper if necessary.
    	if (localDB != null)
            localDB.close();
    }
    
    @Override
    protected void onStop() {
    	// Prevent dialogs from leaking and remaining open.
    	if (noLocationSourceDialog != null && noLocationSourceDialog.isShowing()) {
    		noLocationSourceDialog.cancel();
    		noLocationSourceDialog = null;
    	}
        super.onStop();
    }

    /**
     * Receives the result of the event creation wizard.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d("EventViewer.onActivityResult", "Received result intent. resultCode: " + resultCode);
    	if (requestCode == CreateEvent.REQUEST_CODE_CREATE_EVENT && resultCode == RESULT_OK) {
        	// Show the user the newly created event.
    	    Event event = data.getParcelableExtra("event");
    	    if (event == null)
    		    return;

    	    // If the event should not yet be shown, do not place a marker.
    	    if (!event.isLive()) {
    		    Toast.makeText(this, "Your event will appear closer to its start time.", Toast.LENGTH_LONG).show();
    		    return;
    	    }

    	    // Display the new marker
    	    this.currentEvents.add(event);
    	    Marker m = mMap.addMarker(event.toMarker());
    	    eventMarkerMap.put(m, event.getId());

    	    // Pan and zoom to this new marker.
    	    CameraUpdate viewEvent = CameraUpdateFactory.newLatLngZoom(event.getLocation(), 18);
    	    mMap.animateCamera(viewEvent);

    	    // Show the info window for the new event.
    	    m.showInfoWindow();
    	}
    	if (requestCode == MyEvents.REQUEST_CODE_MY_EVENTS && resultCode == RESULT_OK){
			if (currentLocation != null && isOnline())
			    new getEventsNearLocationAPICaller().execute(currentLocation);
    	}
    }

    private Menu _menu;
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_event_viewer, menu);

        menu.setGroupVisible(R.id.group_viewer_menuitems, !this.initialScreenVisible());

        // Enable and make transparent certain menu items based on location status.
        menu.findItem(R.id.menu_refresh_events).setEnabled(this.currentLocation != null)
                                               .setIcon(this.currentLocation != null ? R.drawable.refresh : R.drawable.refresh_transparent);
        menu.findItem(R.id.menu_create_event).setEnabled(this.currentLocation != null)
                                             .setIcon(this.currentLocation != null ? R.drawable.ic_action_new : R.drawable.ic_action_new_transparent);

        // Keep a reference to the menu for later uses (refresh indicator change).
        this._menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
    	// Close the help menu if it is open as an action is being taken.
    	// Like the USA Today app, this does not mean we remember it has been seen.
    	if (this.helpOpen)
    		findViewById(R.id.help_viewer).setVisibility(View.GONE);
    	
    	int currentId = item.getItemId();
        if (currentId == R.id.menu_create_event) {
        	//Makes sure null cannot get passed as an location
        	if (this.currentLocation == null) {
        		Toast.makeText(this, "Please turn on a location source.", Toast.LENGTH_SHORT).show();
        	    return true;
        	}

			Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);
			createEvent.putExtra("location", this.currentLocation);
			startActivityForResult(createEvent, CreateEvent.REQUEST_CODE_CREATE_EVENT);
			return true;
		} else if (currentId == R.id.menu_refresh_events) {
			// Refresh the event listing.
			if (!isOnline())
				this.displayConnectivityMessage();
			else if (currentLocation != null)
			    new getEventsNearLocationAPICaller().execute(currentLocation);
			return true;
		} else if (currentId == R.id.menu_type_filter) {
			// Show dialog allowing the user to view only certain event types.
			DialogFragment typeFilterDialog = new TypeFilterDialogFragment();
			typeFilterDialog.show(getSupportFragmentManager(), "typeFilterDialog");
			return true;
		} else if (currentId == R.id.menu_settings) {
			// Show the settings activity.
			Intent settings = new Intent(EventViewer.this, Settings.class);
			startActivity(settings);
		} else if (currentId == R.id.menu_viewer_help) {
			// Show the help information view overlay.
			this.helpOpen = true;
            findViewById(R.id.help_viewer).setVisibility(View.VISIBLE);
			return true;
		} else if (currentId == R.id.menu_my_events) {
			// Show a listing of the events we have made.
		    Intent myEvents = new Intent(EventViewer.this, MyEvents.class);
			startActivityForResult(myEvents, MyEvents.REQUEST_CODE_MY_EVENTS);
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
    	savedInstanceState.putParcelable("currentMapLocation", mMap.getCameraPosition().target);
    	savedInstanceState.putFloat("currentMapZoom", mMap.getCameraPosition().zoom);
    	if (currentLocation != null) {
    	    savedInstanceState.putParcelable("currentUserLocation", this.currentLocation);
    	    savedInstanceState.putFloat("currentUserLocationAccuracy", mMap.getMyLocation().getAccuracy());
    	}

    	// Keep the collection of Events when changing configuration.
    	savedInstanceState.putParcelableArrayList("currentEvents", this.currentEvents);

    	// Dialogs get thrown under the bus on configuration changes, so their
        // state must be remembered.
    	if (noLocationSourceDialog != null && noLocationSourceDialog.isShowing())
    		savedInstanceState.putBoolean("noLocationSourceDialogOpen", true);

    	// Save the state of the initial screen.
    	savedInstanceState.putInt("initialScreenVisible", findViewById(R.id.initialScreen).getVisibility());
    	savedInstanceState.putInt("initialNoSourcesVisible", findViewById(R.id.initialNoSources).getVisibility());
    	savedInstanceState.putInt("initialCreateEventVisible", findViewById(R.id.createEventButton).getVisibility());
    	savedInstanceState.putInt("initialViewEventsVisible", findViewById(R.id.initialMainActions).getVisibility());
    	savedInstanceState.putInt("initialLocProgressVisible", findViewById(R.id.initialProgressLayout).getVisibility());

    	// Save the state of the help view.
    	savedInstanceState.putBoolean("helpOpen", this.helpOpen);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Handles the user clicking the `View Events` button on the initial screen.
     */
    public void onViewEventsClick(View v) {
    	Log.d("EventViewer", "User clicked View Events.");
    	
    	final Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    	final View initial = findViewById(R.id.initialScreen);
    	fadeOut.setAnimationListener(new AnimationListener() {
    	    public void onAnimationEnd(final Animation animation) {
    	    	hideInitialScreen();
    	    }

			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}
    	});
    	initial.startAnimation(fadeOut);
    }

    /**
     * Handles the user clicking the `Create Event` button on the initial screen.
     * Hides the initial screen and opens the create event wizard.
     */
    public void onCreateEventClick(View v) {
    	Log.d("EventViewer", "User clicked Create Event.");

		Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);
		createEvent.putExtra("location", this.currentLocation);
		startActivityForResult(createEvent, CreateEvent.REQUEST_CODE_CREATE_EVENT);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    public void run() {
                hideInitialScreen();
		    }
		}, 1000);

    }

    public void onRetryLoadClick(View v) {
    	Log.d("EventViewer", "User clicked retry loading.");
    	
    	this.updateUserLocation();
    }

    public void onEnableSourcesClick(View v) {
    	Log.d("EventViewer", "User clicked enable sources.");

    	startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void hideInitialScreen() {
    	findViewById(R.id.initialScreen).setVisibility(View.GONE);

    	// Prompt the options menu to become visible again.
    	invalidateOptionsMenu();
    }

    private boolean initialScreenVisible() {
    	return findViewById(R.id.initialScreen).getVisibility() == View.VISIBLE;
    }

    /**
     * Closes the help information view.
     */
    public void onCloseHelpClick(View v) {
    	assert this.helpOpen;

    	// Ensure that we remember we have already seen this help.
    	SharedPreferences helpPrefs = getSharedPreferences(Settings.HELP_FILE, 0);
        helpPrefs.edit().putBoolean(Settings.HELP_VIEWER_SEEN, true).commit();

        // Hide the help view.
        findViewById(R.id.help_viewer).setVisibility(View.GONE);
        this.helpOpen = false;
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
    	mMap.setMyLocationEnabled(true);
    	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMapLocation, currentMapZoom));
    	mMap.setOnInfoWindowClickListener(this);
    	mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
    	    public void onCameraChange(CameraPosition arg0) {
    	    	Log.d("EventViewer.onCameraChange", "onCameraChangeListener executed.");

    	    	// We are able to use newLatLngBounds when the map reaches this point in layout.
    	    	if (mapBounds != null) {
    	    		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 50));
    	    		mapBounds = null;
    	    	}
    	    	mMap.setOnCameraChangeListener(null);
    	    }
    	});
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
		eventDetails.putExtra("event", selectedEvent);
		eventDetails.putExtra("userLocation", this.currentLocation);
		startActivity(eventDetails);
	}

    /**
     * Adds the current Events tracked in this activity to the map.
     */
    private void reloadEventMarkers() {
    	if (this.currentEvents == null || this.currentEvents.size() == 0)
    		return;

    	setUpMapIfNeeded();
    	mMap.clear();
        eventMarkerMap.clear();
        for (Event event : this.currentEvents) {
        	// Only display events that match the currently filtered type list.
        	if (filterTypes.contains(event.getType())) {
    		    Marker m = mMap.addMarker(event.toMarker());
    		    eventMarkerMap.put(m, event.getId());
        	}
    	}
    }

    /**
	 * Interface method for receiving a filter list of event types from
	 * a TypeFilterDialogFragment.
	 */
	public void onTypeFilterDialogOKClick(DialogFragment dialog, ArrayList<Event.Type> selectedTypes) {
		Log.d("EventViewer.onDialogOKClick", "Received list of selected types: " + selectedTypes.toString());
		
		// Replace the previous type filter list with the newly created one.
		filterTypes = selectedTypes;
		
		// Force reloading the markers to perform the filter.
		this.reloadEventMarkers();
	}
	
	/**
	 * Interface method for passing the current Type filter to a
	 * TypeFilterDialogFragment for initializing the checkboxes.
	 */
	public ArrayList<Type> receiveCurrentFilterList() {
		return this.filterTypes;
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
    	noLocationSourceDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Enable", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                dialog.cancel();
	        }
	    });
    	noLocationSourceDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
	        }
	    });
    	noLocationSourceDialog.show();
    }
    public void updateUserLocation() {
        LocationFinder locationFinder = new LocationFinder();
        boolean sourcesExist = locationFinder.getLocation(this);
        if (sourcesExist && this.initialScreenVisible()) {
        	// Indicate that we are updating the location.
        	findViewById(R.id.initialNoSources).setVisibility(View.GONE);
        	findViewById(R.id.initialProgressLayout).setVisibility(View.VISIBLE);
        	findViewById(R.id.initialMainActions).setVisibility(View.VISIBLE);
        }
        else {
        	findViewById(R.id.initialNoSources).setVisibility(View.VISIBLE);
        	findViewById(R.id.initialProgressLayout).setVisibility(View.GONE);
        	findViewById(R.id.initialMainActions).setVisibility(View.INVISIBLE);
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
        	if (!this.atSpecifiedLocation)
        	    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));

        	if (initialScreenVisible()) {
				findViewById(R.id.createEventButton).setVisibility(View.VISIBLE);
				findViewById(R.id.initialProgressLayout).setVisibility(View.GONE);
			}

        	if (isOnline()) {
        	    new getEventsNearLocationAPICaller().execute(currentLocation);
        	}

        	// Invalidate the action bar menu to enable location-based actions.
        	invalidateOptionsMenu();
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
	 * Shows a message informing the user that an internet connection is not available.
	 */
	public void displayConnectivityMessage() {
        Toast.makeText(getApplicationContext(),
                       "Please connect to the Internet and try again!", 
                       Toast.LENGTH_SHORT).show();
	}

	/**
	 * Performs an asynchronous API call to find nearby events.
	 */
	private class getEventsNearLocationAPICaller extends AsyncTask<LatLng, Void, ArrayList<Event>> {
		/**
		 * Quick access to the refresh button in the actionbar.
		 */
		MenuItem refreshItem;

		protected void onPreExecute() {
			// Establish progress UI changes.
			if (_menu != null) {
		        refreshItem = _menu.findItem(R.id.menu_refresh_events);
		        if (refreshItem != null)
			        refreshItem.setActionView(R.layout.actionbar_refresh_progress);
			}
		}

		protected ArrayList<Event> doInBackground(LatLng... location) {
			// Perform the network call to retreive nearby events.
			try {
				return APICalls.getEventsNearLocation(location[0]);
			} catch (ConnectTimeoutException cte) {
				Toast.makeText(getApplicationContext(), "Connection could not be established. Please try again later!", Toast.LENGTH_LONG)
			         .show();
				cte.printStackTrace();
			} catch (SocketTimeoutException ste) {
				Toast.makeText(getApplicationContext(), "Issue receiving server data. Please try again later!", Toast.LENGTH_LONG)
		             .show();
				ste.printStackTrace();
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		protected void onPostExecute(ArrayList<Event> result) {
			// Remove progress UI.
			if (refreshItem != null)
			    refreshItem.setActionView(null);
			refreshItem = null;

			// Keep track of these events and populate the map.
			if (result == null)
				return;
			
			if (result.size() == 0 && !initialScreenVisible()) {
                Toast.makeText(getApplicationContext(), "No events found near you!", Toast.LENGTH_LONG)
			         .show();
                return;
			}

			currentEvents = (ArrayList<Event>)result.clone();
			
			if (localDB == null)
				localDB = new LocalDatabase(EventViewer.this);

			// Inform the local cache of these new events.
			localDB.updateLocalEventCache(result);
			
			reloadEventMarkers();
		}
	}
}
