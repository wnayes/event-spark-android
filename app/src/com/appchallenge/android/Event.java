package com.appchallenge.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Representation of an Event, client-side.
 * Can be built either from a JSONObject containing event details
 * or from scratch.
 */
public class Event {
    private String name;
    private String description;
    private String type;
    private long time;
    private LatLng location;

    /**
     * Create an Event from API-generated JSON.
     */
    public Event(String eventJSON) {
        try {
        	JSONObject jsonObject = new JSONObject(eventJSON);
			this.name = jsonObject.get("name").toString();
			this.description = jsonObject.getString("description").toString();
			this.type = jsonObject.getString("type").toString();
			this.time = jsonObject.getLong("time");
			double lat = jsonObject.getJSONObject("location").getDouble("latitude");
			double lng = jsonObject.getJSONObject("location").getDouble("longitude");
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
    public Event(String name, String description, long time, /*String type,*/ LatLng location) {
        this.name = name;
        this.description = description;
        this.location = location;
        //this.type = type;
        this.time = time;
    }
    
    /**
     * @return The name of the Event.
     */
    public String getName() {
    	return this.name;
    }
    
    /**
     * @return The description of the Event.
     */
    public String getDescription() {
    	return this.description;
    }
    
    /**
     * @return The location of the Event as a LatLng object.
     */
    public LatLng getLocation() {
    	return this.location;
    }
    
    /**
     * @return The type of the Event
     */
    //public String getType() {
    //	return this.type;
    //}
    
    /**
     *  @return The time of event milliseconds.
     */
    public long getTime() {
    	return this.time;
    }
    
    /**
     * @return The Event stringified into a JSON object.
     */
    public String toJSON() {
    	JSONObject event = new JSONObject();
    	try {
			event.put("name", this.name);
			event.put("description", this.description);
			//event.put("type", this.type);
			event.put("time", this.time);
			JSONObject locationObj = new JSONObject();
			locationObj.put("latitude", this.location.latitude);
			locationObj.put("longitude", this.location.longitude);
			event.put("location", locationObj);
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
        return new MarkerOptions().title(this.name)
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
