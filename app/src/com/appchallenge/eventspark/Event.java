package com.appchallenge.eventspark;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Representation of an Event, client-side.
 * Can be built either from a JSONObject containing event details or from scratch.
 */
public class Event implements Parcelable {
	
	/** Basic constructor initializing fields to some (invalid) defaults. */
    public Event() {
    	this.id = -1;
    	this.secretId = "";
    	this.title = "";
    	this.description = "";
    	this.type = Type.OTHER;
    	this.userType = UserType.ANONYMOUS;
    	this.userName = "";
    	this.userPicture = "";
    	this.location = null;
    	this.attendance = 0;
    	this.startDate = null;
    	this.endDate = null;
    }

    /** Constructor creating another Event from an existing Event. */
    public Event(Event e) {
		this.id = e.id;
		this.secretId = e.secretId;
		this.title = e.title;
		this.description = e.description;
		this.type = e.type;
		this.location = e.location;
		this.startDate = e.startDate;
		this.endDate = e.endDate;
		this.attendance = e.attendance;
		this.userType = e.userType;
		this.userName = e.userName;
		this.userPicture = e.userPicture;
	}

    /** Create an Event from API-generated JSON. */
    public Event(String eventJSON) {
    	this();
        try {
        	JSONObject jsonObject = new JSONObject(eventJSON);
        	this.secretId = jsonObject.has("secret_id") ? jsonObject.getString("secret_id") : "";
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
			this.userType = UserType.values()[jsonObject.getInt("user_type")];
			this.userName = jsonObject.getString("user_name");
			this.userPicture = jsonObject.getString("user_picture");
		} catch (JSONException e) {
			Log.e(Event.class.toString(), "Failed parsing eventJSON in Event constructor.");
			e.printStackTrace();
		}
    }

    /**
     * @return The internal id of an Event.
     */
    protected int id;
    public int getId() {
    	return this.id;
    }
    public void setId(Integer id) {
    	this.id = id;
    }
    
    /**
     * @return The secret owner id of an Event. Only exists for events a user has made themselves.
     */
    protected String secretId;
    public String getSecretId() {
    	return this.secretId;
    }
    public void setSecretId(String secretId) {
    	this.secretId = secretId;
    }

    /**
     * @return The title of the Event.
     */
    protected String title;
    public String getTitle() {
    	return this.title;
    }
    public void setTitle(String title) {
    	this.title = title;
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
		public int color() {
			switch (this.value) {
			    case 1:
				    return Color.parseColor("#275fac");
			    case 2:
			    	return Color.parseColor("#ac1f23");
			    case 3:
			    	return Color.parseColor("#8b288d");
			    case 4:
			    	return Color.parseColor("#1f9045");
			    case 5:
			    	return Color.parseColor("#ec7524");
			    case 0:
			    default:
			    	return Color.parseColor("#656566");
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
    protected Type type;
    public Type getType() {
		return this.type;
	}
    public void setType(Type type) {
    	this.type = type;
    }

    /**
     * @return The description of the Event.
     */
    protected String description;
    public String getDescription() {
    	return this.description;
    }
    public void setDescription(String description) {
    	this.description = description;
    }

    /**
     * @return The location of the Event as a LatLng object.
     */
    protected LatLng location;
    public LatLng getLocation() {
    	return this.location;
    }
    public void setLocation(LatLng location) {
    	this.location = location;
    }

    /**
     *  @return The start date and time of the event.
     */
    protected Date startDate;
    public Date getStartDate() {
    	return this.startDate;
    }
    public void setStartDate(Date startDate) {
    	this.startDate = startDate;
    }

    /**
     *  @return The end date and time of the event
     */
    protected Date endDate;
    public Date getEndDate() {
    	return this.endDate;
    }
    public void setEndDate(Date endDate) {
    	this.endDate = endDate;
    }
    
    /**
     * @return The number of users that have claimed they will attend
     * the event from our app.
     */
    protected int attendance;
    public int getAttendance() {
    	return this.attendance;
    }
    public void setAttendance(Integer attendance) {
    	this.attendance = attendance;
    }
    
    /**
     * Enum of the different possible user accounts backing an Event.
     */
    public enum UserType {
    	ANONYMOUS(0),
    	GPLUS(1),
    	FACEBOOK(2);
    	
    	private int value;
    	public int getValue() {
    		return value;
    	}
    	UserType(int value) {
    		this.value = value;
    	}
    }
    

    /**
     * Represents what type of user account the Event was created with.
     */
    protected UserType userType;
    public UserType getUserType() {
    	return this.userType;
    }
    public void setUserType(UserType userType) {
    	this.userType = userType;
    }
    
    /**
     * The name of the user who made the Event. Empty if the account was anonymous.
     */
    protected String userName;
    public String getUserName() {
    	return this.userName;
    }
    public void setUserName(String userName) {
    	this.userName = userName;
    }
    
    /**
     * A url pointing to a profile picture for the user who made the Event.
     */
    protected String userPicture;
    public String getUserPicture() {
    	return this.userPicture;
    }
    public void setUserPicture(String picture) {
    	this.userPicture = picture;
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
			event.put("user_type", this.userType.getValue());
			event.put("user_name", this.userName);
			event.put("user_picture", this.userPicture);
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
    	// Determine which icon resource we use based on event type.
    	int markerIcon;
    	boolean isGhost = new Date().before(startDate);
    	if (this.type == Type.ACADEMICS)
    		markerIcon = isGhost ? R.drawable.academics_ghost : R.drawable.academics;
    	else if (this.type == Type.ATHLETICS)
    		markerIcon = isGhost ? R.drawable.athletics_ghost : R.drawable.athletics;
    	else if (this.type == Type.ENTERTAINMENT)
    		markerIcon = isGhost ? R.drawable.entertainment_ghost : R.drawable.entertainment;
    	else if (this.type == Type.PROMOTIONS)
    		markerIcon = isGhost ? R.drawable.promotions_ghost : R.drawable.promotions;
    	else if (this.type == Type.SOCIAL)
    		markerIcon = isGhost ? R.drawable.social_ghost : R.drawable.social;
    	else
    		markerIcon = isGhost ? R.drawable.other_ghost : R.drawable.other;

        return new MarkerOptions().title(this.title)
                                  .snippet("Click to view more info.")
                                  .position(this.location)
                                  .icon(BitmapDescriptorFactory.fromResource(markerIcon));
    }

    /**
     * @return A Google Maps marker representing the Event.
     * @param isDraggable
     */
    public MarkerOptions toMarker(Boolean isDraggable) {
        return this.toMarker().draggable(isDraggable);
    }

    /**
     * Determines if the Event should be shown to users yet.
     * Currently Events starting within 3 hours should be shown.
     * @return Whether the event should be shown on the map.
     */
    public boolean isLive() {
    	if (this.startDate == null)
    		return false;
    	Date today = new Date();
    	return (today.getTime() + 10800000 > this.startDate.getTime());
    }

    /**
     * Returns whether we have ownership of the Event.
     * @return True if the Event has a populated secret_id field.
     */
    public boolean isOurs() {
    	return !this.secretId.trim().equals("");
    }

    // Methods implementing Parcelable.
	public int describeContents() { return 0; }

	/**
	 * Writes the contents of an Event to a Parcel.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.secretId);
		dest.writeString(this.title);
		dest.writeInt(this.type.getValue());
		dest.writeString(this.description);
		dest.writeDouble(this.location.latitude);
		dest.writeDouble(this.location.longitude);
		dest.writeLong(this.startDate.getTime());
		dest.writeLong(this.endDate.getTime());
		dest.writeInt(this.attendance);
		dest.writeInt(this.userType.getValue());
		dest.writeString(this.userName);
		dest.writeString(this.userPicture);

	}
	
	/**
	 * Static field used to regenerate object, individually or as arrays
	 */
	public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
	    public Event createFromParcel(Parcel pc) {
	        return new Event(pc);
	    }
	    public Event[] newArray(int size) {
	        return new Event[size];
	    }
    };

	/**
	 * Constructor to recreate an event from a Parcel.
	 * Must read the contents in the same order they were added in @writeToParcel.
	 * @param pc
	 */
    public Event(Parcel pc){
    	this.id = pc.readInt();
    	this.secretId = pc.readString();
    	this.title = pc.readString();
    	this.type = Type.typeIndices[pc.readInt()];
    	this.description = pc.readString();
    	this.location = new LatLng(pc.readDouble(), pc.readDouble());
    	this.startDate = new Date(pc.readLong());
    	this.endDate = new Date(pc.readLong());
    	this.attendance = pc.readInt();
    	this.userType = UserType.values()[pc.readInt()];
    	this.userName = pc.readString();
    	this.userPicture = pc.readString();
    }
}
