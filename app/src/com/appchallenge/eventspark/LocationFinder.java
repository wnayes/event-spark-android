package com.appchallenge.eventspark;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Main logic for finding the location of the Android device.
 * Searches for 20 seconds for a new value before returning the last
 * known value.
 * Based on: http://stackoverflow.com/a/3145655/1168121
 */
public class LocationFinder {
	/**
	 * Calls GetLastLocation() if no new location is found in 20 seconds.
	 */
    Timer locationTimer;
    int searchDuration = 20000;

    /**
     * Provides access to Android's location providers.
     */
    LocationManager locationManager;

    /**
     * Used to pass location information back to the caller.
     */
    LocationListener listener;

    boolean gpsEnabled = false;
    boolean networkEnabled = false;

    /**
     * Begins the search for location data.
     * @return True if providers are available, false otherwise.
     */
    public boolean getLocation(Context context)
    {
    	listener = (LocationListener)context;
        if (locationManager == null)
        	locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!gpsEnabled && !networkEnabled)
            return false;

        if(gpsEnabled)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
        if(networkEnabled)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        locationTimer = new Timer();
        locationTimer.schedule(new GetLastLocation(), searchDuration);
        return true;
    }

    /**
     * LocationListener for GPS information.
     */
    LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
        	Log.d(this.getClass().getName(), "Selecting GPS location.");
        	locationTimer.cancel();
        	listener.onLocationChanged(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    /**
     * LocationListener for Network information.
     */
    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
        	Log.d(this.getClass().getName(), "Selecting Network location.");
        	locationTimer.cancel();
        	listener.onLocationChanged(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGPS);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    /**
     * Retrieves the last known location as a backup option.
     */
    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
        	locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);

            Location lastNetworkLocation = null;
            Location lastGPSLocation = null;
            if (gpsEnabled)
            	lastGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (networkEnabled)
            	lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // Use the newest value if both location values exist.
            if (lastGPSLocation != null && lastNetworkLocation!=null) {
                if (lastGPSLocation.getTime() > lastNetworkLocation.getTime()) {
                	Log.d(this.getClass().getName(), "Selecting (old) GPS location.");
                	timeoutLocation = lastGPSLocation;
                } else {
                	Log.d(this.getClass().getName(), "Selecting (old) Network location.");
                	timeoutLocation = lastNetworkLocation;
                }
                timerHandler.sendEmptyMessage(0);
                return;
            }

            if (lastGPSLocation != null) {
            	Log.d(this.getClass().getName(), "Selecting (old) GPS location.");
            	timeoutLocation = lastGPSLocation;
            	timerHandler.sendEmptyMessage(0);
                return;
            }

            if (lastNetworkLocation != null) {
            	Log.d(this.getClass().getName(), "Selecting (old) Network location.");
            	timeoutLocation = lastNetworkLocation;
            	timerHandler.sendEmptyMessage(0);
                return;
            }
            
            Log.d(this.getClass().getName(), "Returning null location!");
            timeoutLocation = null;
            timerHandler.sendEmptyMessage(0);
        }
    }
    
    // Use a Handler to send the location to the UI from the Timer.
    private Location timeoutLocation;
    private final Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
        	listener.onLocationChanged(timeoutLocation);
        }
    };
}
