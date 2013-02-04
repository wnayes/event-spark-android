package com.appchallenge.android;

import java.util.Date;

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
        	this.id = jsonObject.getInt("id");
			this.title = jsonObject.get("title").toString();
			//this.type = jsonObject.get("type").toString();
			this.description = jsonObject.getString("description").toString();
			this.startDate = new Date(jsonObject.getLong("start_date") * 1000);
			this.endDate = new Date(jsonObject.getLong("end_date") * 1000);
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
    public Event(String name, String type, String description, Date startDate, Date endDate, LatLng location) {
    	this.id = -1;
        this.title = name;
        this.type = type;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * @return The internal id of an Event.
     */
    private int id;
    public int getId() {
    	return this.id;
    }

    /**
     * @return The title of the Event.
     */
    private String title;
    public String getTitle() {
    	return this.title;
    }
    
    /**
     * @return The type of the event
     */
    private String type = "";
    public String getType() {
		return this.type;
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
     *  @return The start date and time of the event.
     */
    private Date startDate;
    public Date getStartDate() {
    	return this.startDate;
    }

    /**
     *  @return The end date and time of the event
     */
    private Date endDate;
    public Date getEndDate() {
    	return this.endDate;
    }

    /**
     * @return The Event stringified into a JSON object.
     */
    public String toJSON() {
    	JSONObject event = new JSONObject();
    	try {
    		event.put("id", this.id);
			event.put("title", this.title);
			event.put("type", this.type);
			event.put("description", this.description);
			event.put("start_date", this.startDate.getTime() / 1000);
			event.put("end_date", this.endDate.getTime() / 1000);
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
