package com.appchallenge.android;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
			this.type = Type.typeIndices[jsonObject.getInt("type")];
			this.description = jsonObject.getString("description").toString();
			this.startDate = new Date(jsonObject.getLong("start_date") * 1000);
			this.endDate = new Date(jsonObject.getLong("end_date") * 1000);
			double lat = jsonObject.getDouble("latitude");
			double lng = jsonObject.getDouble("longitude");
			this.location = new LatLng(lat, lng);
			this.attendance = jsonObject.getInt("attending");
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
    public Event(String name, Type type, String description, Date startDate, Date endDate, LatLng location) {
    	// Defaults
    	this.id = -1;
    	this.attendance = 1;

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
     * Enum of the different possible types of events.
     */
    public enum Type {
    	ACADEMICS(1),
    	ATHLETICS(2),
    	ENTERTAINMENT(3),
    	PROMOTIONS(4),
    	SOCIAL(5),
    	OTHER(0);

    	// Types are given an integer constant representation to be stored in
    	// the backend database.
    	private int value;
    	public int getValue() {
            return value;
        }
		Type(int value) {
    		this.value = value;
    	}

		/**
		 * Gives access to the enumerated types by index.
		 */
		public static Type[] typeIndices = new Type[] { OTHER, ACADEMICS, ATHLETICS, ENTERTAINMENT, PROMOTIONS, SOCIAL };
		
		/**
		 * Each type is associated with a unique marker color on the map.
		 */
		public float color() {
			switch (this.value) {
			    case 1:
				    return BitmapDescriptorFactory.HUE_BLUE;
			    case 2:
			    	return BitmapDescriptorFactory.HUE_ORANGE;
			    case 3:
			    	return BitmapDescriptorFactory.HUE_MAGENTA;
			    case 4:
			    	return BitmapDescriptorFactory.HUE_GREEN;
			    case 5:
			    	return BitmapDescriptorFactory.HUE_YELLOW;
			    case 0:
			    default:
			    	return BitmapDescriptorFactory.HUE_RED;
		    }
		}

		@Override
		public String toString() {
			switch (this.value) {
			    case 1:
			    	return "Academics";
			    case 2:
			    	return "Athletics";
			    case 3:
			    	return "Entertainment";
			    case 4:
			    	return "Promotions";
			    case 5:
			    	return "Social";
			    case 0:
			    	return "Other";
			    default:
			    	return "Unknown";
			}
		}
    }
    
    /**
     * @return The type of the event
     */
    private Type type;
    public Type getType() {
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
     * @return The number of users that have claimed they will attend
     * the event from our app.
     */
    private int attendance;
    public int getAttendance() {
    	return this.attendance;
    }

    /**
     * @return The Event stringified into a JSON object.
     */
    public String toJSON() {
    	JSONObject event = new JSONObject();
    	try {
    		event.put("id", this.id);
			event.put("title", this.title);
			event.put("type", this.type.getValue());
			event.put("description", this.description);
			event.put("start_date", this.startDate.getTime() / 1000);
			event.put("end_date", this.endDate.getTime() / 1000);
			event.put("latitude", this.location.latitude);
			event.put("longitude", this.location.longitude);
			event.put("attending", this.attendance);
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
                                  .position(this.location)
                                  .icon(BitmapDescriptorFactory.defaultMarker(this.type.color()));
    }

    /**
     * @return A Google Maps marker representing the Event.
     * @param isDraggable
     */
    public MarkerOptions toMarker(Boolean isDraggable) {
        return this.toMarker().draggable(isDraggable);
    }
}
