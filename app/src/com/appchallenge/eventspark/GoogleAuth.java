package com.appchallenge.eventspark;

import java.io.IOException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


public class GoogleAuth {
	Context mActivity;

	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

	// Activity request codes to identify results.
	static final int REQUEST_CODE_GOOGLE_PLUS_ACCOUNTNAME = 1001;
	static final int REQUEST_CODE_GOOGLE_PLUS_TOKEN = 1002;

	public GoogleAuth(Context activity) {
		mActivity = activity;
	}

	/**
	 * Returns an intent to load a picker for Google+ accounts.
	 * The calling Activity should use this with startActivityForResult.
	 */
	static public Intent getAccountPickerIntent() {
		return AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
		                                            false, null, null, null, null);
	}

	/**
     * Attepts to get a Google+ token. The activity must implement
     * an interface to receive the token at a later time.
     */
    public void getToken(String accountName) {
	    new getTokenTask().execute(accountName);
	}

	/**
	 * Downloads the user's token asynchronously.
	 */
	private class getTokenTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {}

		protected String doInBackground(String... accountname) {
			try {
		        return GoogleAuthUtil.getToken(mActivity, accountname[0], SCOPE);
		    } catch (GooglePlayServicesAvailabilityException playEx) {
		        // GooglePlayServices.apk is either old, disabled, or not present.
		        Log.e("GoogleAuth.getTokenTask", ((Integer)playEx.getConnectionStatusCode()).toString());
		    } catch (UserRecoverableAuthException userRecoverableException) {
		        // Forward the user to the appropriate activity, this can be due to .
		        ((Activity)mActivity).startActivityForResult(userRecoverableException.getIntent(), REQUEST_CODE_GOOGLE_PLUS_TOKEN);
		    	Log.e("GoogleAuth.getTokenTask", "Unable to authenticate, but the user can fix this.");
		    } catch (GoogleAuthException fatalException) {
		        Log.e("GoogleAuth.getTokenTask", "Unrecoverable error " + fatalException.getMessage(), fatalException);
		    } catch (IOException e) {
		    	Log.e("GoogleAuth.getTokenTask", "IOException");
				e.printStackTrace();
			}
		    return null;
		}


		protected void onPostExecute(String result) {
            // Call method on waiting activity.
			((CreateEventInterface)mActivity).setToken(result);
		}
	}
}