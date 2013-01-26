package com.appchallenge.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Representation of an Event, client-side.
 * Can be built either from a JSONObject containing event details or from scratch.
 */
public class Event {
    /**
     * Create an Event from API-generated JSON.
     */
    public Event(String eventJSON) {
        try {
        	JSONObject jsonObject = new JSONObject(eventJSON);
        	if (jsonObject.has("event"))
        		jsonObject = jsonObject.getJSONObject("event");
			this.title = jsonObject.get("title").toString();
			this.description = jsonObject.getString("description").toString();
			this.startTime = jsonObject.getLong("start_date");
			this.endTime = jsonObject.getLong("end_date");
			double lat = jsonObject.getDouble("latitude");
			double lng = jsonObject.getDouble("longitude");
			this.location = new LatLng(lat, lng);
		} catch (JSONException e) {
			Log.e(Event.class.toString(), "Failed parsing eventJSON in Event constructor.");
			e.printStackTrace();
		}
    }

    /**
     * Create a new Event object from details.
     * @param name
     * @param description
     * @param time
     * @param type
     * @param location
     */
    public Event(String name, String description, long startTime, long endTime,/*String type,*/ LatLng location) {
        this.title = name;
        this.description = description;
        this.location = location;
        //this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return The title of the Event.
     */
    private String title;
    public String getTitle() {
    	return this.title;
    }

    /**
     * @return The description of the Event.
     */
    private String description;
    public String getDescription() {
    	return this.description;
    }

    /**
     * @return The location of the Event as a LatLng object.
     */
    private LatLng location;
    public LatLng getLocation() {
    	return this.location;
    }

    /**
     *  @return The start time of event in seconds.
     */
    private long startTime;
    public long getStartTime() {
    	return this.startTime;
    }

    /**
     *  @return The end time in seconds
     */
    private long endTime;
    public long getEndTime() {
    	return this.endTime;
    }

    /**
     * @return The Event stringified into a JSON object.
     */
    public String toJSON() {
    	JSONObject event = new JSONObject();
    	try {
			event.put("title", this.title);
			event.put("description", this.description);
			event.put("start_date", this.startTime);
			event.put("end_date", this.endTime);
			event.put("latitude", this.location.latitude);
			event.put("longitude", this.location.longitude);
			
		} catch (JSONException e) {
			Log.e(Event.class.toString(), "Could not stringify existing Event object!");
			e.printStackTrace();
			return null;
		}

    	return event.toString();
    }

    /**
     * @return A Google Maps marker representing the Event.
     */
    public MarkerOptions toMarker() {
        return new MarkerOptions().title(this.title)
                                  .snippet(this.description)
                                  .position(this.location);
    }

    /**
     * @return A Google Maps marker representing the Event.
     * @param isDraggable
     */
    public MarkerOptions toMarker(Boolean isDraggable) {
        return this.toMarker().draggable(isDraggable);
    }
}
