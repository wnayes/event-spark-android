package com.appchallenge.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class WelcomeDialogFragment extends DialogFragment {
	
	private static final String WELCOME_DIALOG = "Welcome";
	private static final String WELCOME_KEY = "DISPLAY";
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		builder.setView(inflater.inflate(R.layout.welcome_dialog, null));
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton(R.string.welcome_dialog_never_display,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences welcomeIndicator = getActivity().getBaseContext().getSharedPreferences(WELCOME_DIALOG, 0);
				String indicator = welcomeIndicator.getString(WELCOME_KEY, "");
				if (indicator.length() == 0) {
				    SharedPreferences.Editor editor = welcomeIndicator.edit();
				    editor.putString(WELCOME_KEY, "no");
				    editor.commit();
				    dialog.dismiss();
				}
			}
		}); 

		return builder.create();
	}
}
