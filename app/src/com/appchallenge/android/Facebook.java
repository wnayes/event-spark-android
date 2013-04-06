package com.appchallenge.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;

public class Facebook {

	private static List<String> permissions = new ArrayList<String>();
	
	public static void startSession(Context context) {
		setList();
		final Activity activity = (Activity) context;
	    Session session = Session.getActiveSession();
	    if (session == null) {
	    	Session.openActiveSession(activity, true, new Session.StatusCallback() {
		        @SuppressWarnings("unchecked")
		        @Override
		            public void call(Session session, SessionState state, Exception exception) {
			        if (state.isOpened() && (state.equals(SessionState.OPENED_TOKEN_UPDATED) || state.equals(SessionState.OPENED))) {
				        if (!Arrays.asList(session.getPermissions()).contains("publish_actions")) {
				            session.requestNewPublishPermissions(new Session.NewPermissionsRequest(activity, permissions));
				            Log.d("ShareDialogFragment", "Requesting Share Permissions");
				        }
			        }
		        }
	        });
	    }
	}
	
	private static void setList() {
		permissions.clear();
		permissions.add("publish_actions");
		permissions.add("user_photos");
		permissions.add("read_friendlists");
	}
	
	public static String getToken() {
		Session currentSession = Session.getActiveSession();
		if (currentSession != null) {
			return currentSession.getAccessToken();
		}
		return "";
	}
	
	public Session getSession() {
		return Session.getActiveSession();
	}
}
