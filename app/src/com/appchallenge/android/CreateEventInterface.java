package com.appchallenge.android;

import java.util.Date;
import com.google.android.gms.maps.model.LatLng;

/**
 * Interface that the CreateEvent wizard implements to allow the
 * individual page fragments to communicate with each other.
 */
public interface CreateEventInterface {
	public String getEventTitle();
	public void setTitle(String name);
	
	public String getDescription();
	public void setDescription(String description);

	public Date getStartDate();
	public void setStartDate(Date date);
	
	public Date getEndDate();
	public void setEndDate(Date date);
	
	public LatLng getLocation();
	public void setLocation(LatLng location);
	
	public String getType();
	public void setType(String type);

}
