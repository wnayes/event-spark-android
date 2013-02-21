package com.appchallenge.android;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Provides access to the user's personal user ID.
 * Based on the "Identifying Android Applications" post.
 * @see <a href="http://android-developers.blogspot.com/2011/03/identifying-app-installations.html">Identifying Android Applications</a>
 */
public class Identity {
	private static final String USER_ID_PREF_FILE = "UserId";
	private static final String USER_ID_KEY_NAME = "user_id";

    public static String getUserId(Context context) {
    	// Retrieve the user's ID from SharedPreferences.
    	SharedPreferences userIdStorage = context.getSharedPreferences(USER_ID_PREF_FILE, 0);
    	String userId = userIdStorage.getString(USER_ID_KEY_NAME, "");
    	
    	// If this is the first time requesting an identity, create one.
    	if (userId.isEmpty()) {
    		userId = UUID.randomUUID().toString();
    		SharedPreferences.Editor userIdEdit = userIdStorage.edit();
    		userIdEdit.putString(USER_ID_KEY_NAME, userId);
    		userIdEdit.apply();
    	}
    	
    	return userId;
    }
}
