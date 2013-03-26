package com.appchallenge.android;

import java.util.ArrayList;

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

    private static final int DATABASE_VERSION = 6;

    /** Name of the database we use to store our tables. */
    private static final String DATABASE_NAME = "eventLocalDatabase";

    /** Table for keeping local track of events we have attended. */
    private static final String LOCAL_ATTENDANCE_TABLE_NAME = "local_attendance";

    /** Column names in local_attendance. */
    private static final String KEY_ID = "id";

    /** SQLITE command for creating the local attendance table. */
    private static final String LOCAL_ATTENDANCE_TABLE_CREATE = "CREATE TABLE " + LOCAL_ATTENDANCE_TABLE_NAME + " (" +
                                                                 KEY_ID + " INTEGER PRIMARY KEY);";
    
    /* *********************************** */
    /** Table for keeping a local "cache" of events we have downloaded. */
    private static final String EVENT_CACHE_TABLE_NAME = "event_cache";

    /** Column names in event_cache. */
    //private static final String KEY_ID = "id";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_STARTDATE = "start_date";
    private static final String KEY_ENDDATE = "end_date";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ATTENDING = "attending";
    private static final String KEY_SECRETID = "secret_id";

    /** SQLITE command for creating the event_cache table. */
    private static final String EVENT_CACHE_TABLE_CREATE = "CREATE TABLE " + EVENT_CACHE_TABLE_NAME + " (" +
                                                            KEY_ID + " INTEGER PRIMARY KEY, " +
                                                            KEY_DESCRIPTION + " TEXT, " +
                                                            KEY_LONGITUDE + " REAL, " +
                                                            KEY_LATITUDE + " REAL, " +
                                                            KEY_STARTDATE + " INTEGER, " +
                                                            KEY_ENDDATE + " INTEGER, " +
                                                            KEY_TYPE + " INTEGER, " +
                                                            KEY_ATTENDING + " INTEGER, " +
                                                            KEY_SECRETID + " TEXT);";

    LocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.d("LocalDatabase.onCreate", "localDB tables being recreated.");

    	// Create the tables for the database.
        db.execSQL(LOCAL_ATTENDANCE_TABLE_CREATE);
        db.execSQL(EVENT_CACHE_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("LocalDatabase.onUpgrade", "localDB tables being dropped.");

		// Drop the existing copy of the table and create it again.
		db.execSQL("DROP TABLE IF EXISTS " + LOCAL_ATTENDANCE_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + EVENT_CACHE_TABLE_NAME);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Downgrades are performed in the same manner as upgrades.
		this.onUpgrade(db, oldVersion, newVersion);
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

		String eventId = ((Integer)(event.getId())).toString();

		// Query if we already have this event in the local cache.
        Cursor result = db.query(EVENT_CACHE_TABLE_NAME,
                                 new String[] {KEY_ID},
                                 "id = ?",
                                 new String[] { eventId },
                                 null, null, null);
        int rowCount = result.getCount();
        result.close();

        assert rowCount < 2;
        if (rowCount == 1) {
        	// Update the entry with the new secret_id.
        	ContentValues values = new ContentValues();
        	values.put(KEY_SECRETID, event.getSecretId());
        	db.update(EVENT_CACHE_TABLE_NAME, values, "id = ?", new String[] { eventId });;
        }
        else if (rowCount == 0) {
        	// Insert the event into the local cache.
        	ContentValues values = new ContentValues();
			values.put(KEY_ID, event.getId());
			values.put(KEY_DESCRIPTION, event.getDescription());
			values.put(KEY_LONGITUDE, event.getLocation().longitude);
			values.put(KEY_LATITUDE, event.getLocation().latitude);
			values.put(KEY_STARTDATE, event.getStartDate().getTime() / 1000);
			values.put(KEY_ENDDATE, event.getEndDate().getTime() / 1000);
			values.put(KEY_TYPE, event.getType().getValue());
			values.put(KEY_ATTENDING, event.getAttendance());
			values.put(KEY_SECRETID, event.getSecretId());
			
			db.insert(EVENT_CACHE_TABLE_NAME, null, values);
        }

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

        Cursor result = db.query(EVENT_CACHE_TABLE_NAME,
                                 new String[] {KEY_SECRETID},
                                 "id = ?",
                                 new String[] {id.toString()},
                                 null, null, null);

        // Read the secretId if it was found.
        if (result.moveToFirst())
        	secretId = result.getString(0);

        result.close();
        db.close();
        return secretId;
	}

	/**
	 * Adds an event ID to our list of events we have attended.
	 * @param eventId The event we have just indicated attendance of.
	 */
	public void trackAttendance(Integer eventId) {
		SQLiteDatabase db = this.getWritableDatabase();

		if (eventId < 1) {
    		Log.e("LocalDatabase.trackAttendance", "Given id was not valid.");
    		db.close();
		}

		ContentValues values = new ContentValues();
		values.put(KEY_ID, eventId);
		db.insert(LOCAL_ATTENDANCE_TABLE_NAME, null, values);
		db.close();
	}

	/**
	 * Checks if we know that we have previously said we would attend an event.
	 * @param eventId The event id.
	 * @return True if we know we have attended this event already.
	 */
	public Boolean getAttendanceStatus(Integer eventId) {
		SQLiteDatabase db = this.getWritableDatabase();

		if (eventId < 1) {
    		Log.e("LocalDatabase.trackAttendance", "Given id was not valid.");
    		db.close();
    		return false;
		}

		// Query to see if we have an entry with the id already.
        Cursor result = db.query(LOCAL_ATTENDANCE_TABLE_NAME,
                                 new String[] {KEY_ID},
                                 "id = ?",
                                 new String[] {eventId.toString()},
                                 null, null, null);

        if (result.moveToFirst()) {
        	result.close();
        	db.close();
        	return true;
        }

        result.close();
        db.close();
        return false;
	}

	/**
	 * Inserts any new events that we have not yet cached.
	 * @param latestEvents An ArrayList received from the backend.
	 * @return A sublist containing events found to be actually new.
	 */
	public ArrayList<Event> updateLocalEventCache(ArrayList<Event> latestEvents) {
		SQLiteDatabase db = this.getWritableDatabase();

		// Read a list of the ids we currently have cached.
		Cursor result = db.rawQuery("SELECT id FROM " + EVENT_CACHE_TABLE_NAME, null);
		
		ArrayList<Integer> cachedEventIds = new ArrayList<Integer>();
		if (result.moveToFirst()) {
			do {
				cachedEventIds.add(result.getInt(0));
			} while (result.moveToNext());
		}
		result.close();
		
		// Check if we have found a new event by id.
		for (int i = latestEvents.size() - 1; i >= 0 ; --i) {
			int newId = latestEvents.get(i).getId();
			for (int j = cachedEventIds.size() - 1; j >= 0 ; --j) {
				if (cachedEventIds.get(j) == newId)
					latestEvents.remove(i);
			}
		}
		
		// The remaining events in `latestEvents` have new ids, so they should be added
		// to the event cache.
		for (Event newEvent : latestEvents) {
			ContentValues values = new ContentValues();
			values.put(KEY_ID, newEvent.getId());
			values.put(KEY_DESCRIPTION, newEvent.getDescription());
			values.put(KEY_LONGITUDE, newEvent.getLocation().longitude);
			values.put(KEY_LATITUDE, newEvent.getLocation().latitude);
			values.put(KEY_STARTDATE, newEvent.getStartDate().getTime() / 1000);
			values.put(KEY_ENDDATE, newEvent.getEndDate().getTime() / 1000);
			values.put(KEY_TYPE, newEvent.getType().getValue());
			values.put(KEY_ATTENDING, newEvent.getAttendance());
			
			db.insert(EVENT_CACHE_TABLE_NAME, null, values);
		}
		
		db.close();
		return latestEvents;
	}
}
