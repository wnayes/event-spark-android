package com.appchallenge.android;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity {

	// SharedPreferences files and keys.

	// Preferences for the help dialogs.
	final static String HELP_FILE = "Help";
	final static String HELP_VIEWER_SEEN = "HELP_VIEWER_SEEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    
    @Override
    protected void onPause() {
    	Log.d("Settings.onPause", "Settings activity has been paused.");
    	// Apply any settings changes.

    	Intent intent = new Intent(this, NotificationService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

		// Cancel any notification alarms we may have set. If the settings specify it,
		// we will add a new alarm below.
		alarm.cancel(pintent);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("notificationsEnabled", false)) {
			// Start the NotificationService on a fixed interval.
			Integer interval = Integer.parseInt(prefs.getString("notificationCheckInterval", "300000"));
			Log.d("notificationCheckInterval", interval.toString());
			Calendar cal = Calendar.getInstance();
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + interval, interval, pintent); 
		}

    	super.onPause();
    }
}
