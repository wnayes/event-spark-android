package com.appchallenge.android;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import android.os.Bundle;
import android.view.View;

public class Welcome extends SherlockActivity {
		
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.welcome_layout);
	}
	
	
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_event_viewer, menu);
        
        // Keep a reference to the menu for later uses (refresh indicator change).
        return true;
    }
	
	public void onButtonPress(View view) {
		Welcome.this.finish();
	}
}
