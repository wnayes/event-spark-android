package com.appchallenge.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Provides access to a local database for storing various information.
 * 
 * Name: eventLocalDatabase
 * Tables:
 *   users_events
 */
public class LocalDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    
    /** Name of the database we use to store our tables. */
    private static final String DATABASE_NAME = "eventLocalDatabase";

    /** Table for storing the owner_ids of events we have created. */
    private static final String USERS_EVENTS_TABLE_NAME = "users_events";
    
    /** Column names in users_events. */
    private static final String KEY_ID = "id";
    private static final String KEY_OWNERID = "owner_id";
   
    
    /** SQLITE command for creating the users_events table. */
    private static final String USERS_EVENTS_TABLE_CREATE = "CREATE TABLE " + USERS_EVENTS_TABLE_NAME + " (" +
                                                             KEY_ID + " INTEGER PRIMARY KEY, " +
                                                             KEY_OWNERID + " TEXT);";

    LocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	// Create the tables for the database.
        db.execSQL(USERS_EVENTS_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop the existing copy of the table and create it again.
		db.execSQL("DROP TABLE IF EXISTS " + USERS_EVENTS_TABLE_NAME);
		onCreate(db);
	}

	/** CRUD (Create, Read, Update, Delete) Procedures */

    /**
     * Adds the owner_id of a given event to our database.
     * @param event The event we own.
     */
	public void takeOwnership(Event event) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		if (event.getSecretId().length() == 0 || event.getId() < 1) {
    		Log.e("EventViewer.onActivityResult", "Event does not have the proper private members.");
    		db.close();
    		return;
    	}

		ContentValues values = new ContentValues();
		values.put(KEY_ID, event.getId());
		values.put(KEY_OWNERID, event.getSecretId());

		db.insert(USERS_EVENTS_TABLE_NAME, null, values);
		db.close();
	}

	/**
	 * Locates the secret id of an event we own.
	 * @param event
	 * @return The secret id as a string, or the empty string if we do not have ownership.
	 */
	public String getEventSecretId(Event event) {
		SQLiteDatabase db = this.getWritableDatabase();
		String secretId = "";

		Integer id = event.getId();
		if (id < 1) {
    		Log.e("EventViewer.onActivityResult", "Event needs an id.");
    		db.close();
    		return secretId;
    	}

        Cursor result = db.query(USERS_EVENTS_TABLE_NAME,
                                 new String[] {KEY_OWNERID},
                                 "id = ?",
                                 new String[] {id.toString()},
                                 null, null, null);

        // Read the secretId if it was found.
        if (result.moveToFirst())
        	secretId = result.getString(0);

        db.close();
        return secretId;
	}
	/**
	 * 
	 * @param event
	 * @param user_id
	 * @return An int depending on what happened. 1 means success 0 means already in table
	 *         -1 is an error
	 */

}
