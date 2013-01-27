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

	public Date getDate();
	public void setDate(Date date);
	
	public float getDuration();
	public void setDuration(float duration);

	public LatLng getLocation();
	public void setLocation(LatLng location);

}
