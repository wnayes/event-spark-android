package com.appchallenge.android;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class NotificationService extends Service implements LocationListener {

	@Override
	public void onCreate() {
	    super.onCreate();
	    Log.d("NotificationService.onCreate", "NotificationService starting.");
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("NotificationService.onStartCommand", "NotificationService received command.");

		LocationFinder locationFinder = new LocationFinder();
        boolean sourcesExist = locationFinder.getLocation(this);
        if (!sourcesExist) {
        	Log.d("NotificationService.onStartCommand", "NotificationService had no location sources!");
        	stopSelf();
        }

        return Service.START_STICKY;
    }

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    Log.d("NotificationService.onDestroy", "NotificationService stopping.");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We do not need to bind anything to this service.
		return null;
	}

	// Method implementations for LocationListener.
	@Override
	public void onLocationChanged(Location arg0) {
		Log.d("NotificationService.onLocationChanged", "NotificationService received location information.");
		stopSelf();
	}

	public void onProviderDisabled(String arg0) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
