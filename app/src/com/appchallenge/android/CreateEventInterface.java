package com.appchallenge.android;

import java.util.Date;
import com.google.android.gms.maps.model.LatLng;

/**
 * Interface that the CreateEvent wizard implements to allow the
 * individual page fragments to communicate with each other.
 */
public interface CreateEventInterface {
	public String getName();
	public void setName(String name);
	
	public String getDescription();
	public void setDescription(String description);

	public Date getDate();
	public void setDate(Date date);

	public LatLng getLocation();
	public void setLocation(LatLng location);

}
