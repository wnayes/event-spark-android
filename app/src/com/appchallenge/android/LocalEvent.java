package com.appchallenge.android;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

/**
 * Represents an Event that we have created ourselves rather than received from the backend.
 */
public class LocalEvent extends Event {
	LocalEvent(Integer id, String secretId, String title, String description,
			   Type type, LatLng location, Date startDate, Date endDate, Integer attendance) {
		this.id = id;
		this.secretId = secretId;
		this.title = title;
		this.description = description;
		this.type = type;
		this.location = location;
		this.startDate = startDate;
		this.endDate = endDate;
		this.attendance = attendance;
	}

	LocalEvent(Event e) {
		this.id = e.id;
		this.secretId = e.secretId;
		this.title = e.title;
		this.description = e.description;
		this.type = e.type;
		this.location = e.location;
		this.startDate = e.startDate;
		this.endDate = e.endDate;
		this.attendance = e.attendance;
	}

	LocalEvent() {
		super();
	}

    public void setId(Integer id) {
    	this.id = id;
    }

    public void setTitle(String title) {
    	this.title = title;
    }

    public void setDescription(String description) {
    	this.description = description;
    }

    public void setType(Type type) {
    	this.type = type;
    }

    public void setLocation(LatLng location) {
    	this.location = location;
    }

    public void setStartDate(Date startDate) {
    	this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
    	this.endDate = endDate;
    }

    public void setAttendance(Integer attendance) {
    	this.attendance = attendance;
    }
}
