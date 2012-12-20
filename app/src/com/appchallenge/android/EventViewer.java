package com.appchallenge.android;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;


 // This shows how to create a simple activity with a map and a marker on the map.
 // Notice how we deal with the possibility that the Google Play services APK is not
 // installed/enabled/updated on a user's device.
public class EventViewer extends android.support.v4.app.FragmentActivity implements LocationListener {
    // Note that this may be null if the Google Play services APK is not available.
    private GoogleMap mMap;

    private LocationManager locationManager;
    private String provider;
    private LatLng currentLocation;
    private float currentZoom = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);
        setUpMapIfNeeded();
        mMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom));

        // Handle location detection on startup.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Use default location criteria
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        // Apply the first location value.
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            System.out.println("No location provider found!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Receive updates every 10 seconds when differing by 8km or more.
        locationManager.requestLocationUpdates(provider, 10000, 8, this);
        setUpMapIfNeeded();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        locationManager.removeUpdates(this);
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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
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
    }

	@Override
	public void onLocationChanged(Location location) {
		this.currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
		
		// Keep the user's zoom if they have changed it.
		if (Math.abs(currentZoom - mMap.getCameraPosition().zoom) > 0.2)
			currentZoom = mMap.getCameraPosition().zoom;

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoom));
		Toast.makeText(this, "Received significant LatLng change", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
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
}
