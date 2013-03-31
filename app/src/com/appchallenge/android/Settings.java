package com.appchallenge.android;

import java.util.Calendar;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity {

	// SharedPreferences files and keys.

	// Preferences for the help dialogs.
	final static String HELP_FILE = "Help";
	final static String HELP_VIEWER_SEEN = "HELP_VIEWER_SEEN";
	//private PendingAction pendingAction = PendingAction.NONE;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            //onSessionStateChange(session, state, exception);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference button = (Preference)findPreference("button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) { 
                            loginFacebook();  
                            return true;
                        }
                    });
        Session session = Session.getActiveSession();
        if (session != null)
    	    		button.setTitle("Log Out of Facebook");
    	    	else
    	    		button.setTitle("Log Into Facebook");
    	    
    }
    
    protected void loginFacebook() {
    	Preference button = (Preference)findPreference("button");
    	Session session = Session.getActiveSession();
    	if (session == null) {
    		Session.openActiveSession(this, true, new Session.StatusCallback() {

        	    // callback when session changes state
        	    @Override
        	    public void call(Session session, SessionState state, Exception exception) {
        	    	if (session.isOpened()) {

        	            // make request to the /me API
        	            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

        	              // callback after Graph API response with user object
        	              @Override
        	              public void onCompleted(GraphUser user, Response response) {
        	                if (user != null) {
        	                  Log.d("stuffs", "stuffs");
        	                }
        	              }
        	            });
        	    	}
        	    
        	  }
        });
    	button.setTitle("Log Out of Facebook");
    	}
     
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
