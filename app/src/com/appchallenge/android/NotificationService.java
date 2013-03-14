package com.appchallenge.android;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.conn.ConnectTimeoutException;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class NotificationService extends Service implements LocationListener {
	/**
	 * Storage for the events we receive from the backend.
	 */
	ArrayList<Event> latestEvents;

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
	public void onLocationChanged(Location loc) {
		Log.d("NotificationService.onLocationChanged", "NotificationService received location information.");

		// We are in a Service so this blocking call should not cause UI responsiveness issues.
		// If it does, 
		getEventsNearLocationAPICaller caller = new getEventsNearLocationAPICaller();
		try {
			latestEvents = caller.execute(new LatLng(loc.getLatitude(), loc.getLongitude())).get();
		}
        catch (InterruptedException e) { e.printStackTrace(); stopSelf(); }
        catch (ExecutionException e) { e.printStackTrace(); stopSelf(); }

		if (latestEvents == null || latestEvents.size() == 0) {
			stopSelf();
			return;
		}

		Log.d("NotificationService.onLocationChanged", "NotificationService received new event information.");

		stopSelf();
	}

	public void onProviderDisabled(String arg0) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	/**
	 * Performs an asynchronous API call to find nearby events.
	 */
	private class getEventsNearLocationAPICaller extends AsyncTask<LatLng, Void, ArrayList<Event>> {

		protected void onPreExecute() {}

		protected ArrayList<Event> doInBackground(LatLng... location) {
			// Perform the network call to retreive nearby events.
			try {
				return APICalls.getEventsNearLocation(location[0]);
			} catch (ConnectTimeoutException cte) {
				Log.e("getEventsNearLocationAPICaller", "Connection could not be established. Please try again later!");
				cte.printStackTrace();
			} catch (SocketTimeoutException ste) {
				Log.e("getEventsNearLocationAPICaller",  "Issue receiving server data. Please try again later!");
				ste.printStackTrace();
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		protected void onPostExecute(ArrayList<Event> result) {
			if (result == null || result.size() == 0)
				return;

			latestEvents = (ArrayList<Event>)result.clone();
		}
	}
}
