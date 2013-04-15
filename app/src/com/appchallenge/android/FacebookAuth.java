package com.appchallenge.android;

import java.util.Arrays;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;

public class FacebookAuth {
		
	public void startSession(Context context) {
		SessionTracker sessionTracker = new SessionTracker(context, new StatusCallback(){
			
			@Override
			public void call(Session session, SessionState state, Exception excep){}
		});
		String appId = Utility.getMetadataApplicationId(context);
		Log.d("App ID", appId);
		Session currentSession = sessionTracker.getSession();
		
		if(currentSession == null || currentSession.getState().isClosed()) {
			sessionTracker.setSession(null);
			Session session = new Session.Builder(context).setApplicationId(appId).build();
			Session.setActiveSession(session);
			currentSession = session;
		}
		
		if (!currentSession.isOpened()) {
			Session.OpenRequest openRequest = new Session.OpenRequest((Activity) context);
			
			openRequest.setDefaultAudience(SessionDefaultAudience.FRIENDS);
			openRequest.setPermissions(Arrays.asList("publish_actions"));
			openRequest.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
			
			currentSession.openForPublish(openRequest);
		}
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
		return session;
	}
}
