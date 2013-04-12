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
	private static List<String> photoPermissions = new ArrayList<String>();
	private static boolean write = true;
	private static boolean read = true;
	public static void startSession(Context context) {
		setList();
		final Activity activity = (Activity) context;
	    Session session = Session.getActiveSession();
	    Log.d("Facebook", "Checking and Starting Session");
	    if (session == null || session.isClosed()) {
	    	Session.openActiveSession(activity, true, new Session.StatusCallback() {
		        @SuppressWarnings("unchecked")
		        @Override
		            public void call(Session session, SessionState state, Exception exception) {
			        if (state.isOpened() || (state.equals(SessionState.OPENED_TOKEN_UPDATED) || state.equals(SessionState.OPENED))) {
				        if (!Arrays.asList(session.getPermissions()).contains("publish_actions") ||
				        		!Arrays.asList(session.getPermissions()).contains("user_photos")) {
				        	if (write) {
				                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(activity, permissions).setCallback(this));
				                write = false;
				                read = false;
				        	}
				        	if (read) {
				                session.requestNewReadPermissions(new Session.NewPermissionsRequest(activity, photoPermissions).setCallback(this));
				                Log.d("ShareDialogFragment", "Requesting Share Permissions");
				                read = false;
				        	}
				        }
			        }
		        }
	        });
	    }
	}
	
	private static void setList() {
		permissions.clear();
		photoPermissions.clear();
		write = true;
		read = true;
		permissions.add("publish_actions");
		photoPermissions.add("user_photos");
	}
	
	public static String getToken() {
		Session currentSession = Session.getActiveSession();
		if (currentSession != null) {
			Log.d("Facebook getToken()", currentSession.getAccessToken());
			return currentSession.getAccessToken();
		}
		return "";
	}
	
	public Session getSession(Context context) {
		Session session = Session.getActiveSession();
		if (session == null) {
			startSession(context);
		}
		session = Session.getActiveSession();
		return session;
	}
}
