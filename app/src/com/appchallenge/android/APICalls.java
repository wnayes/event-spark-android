package com.appchallenge.android;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.util.Log;

/**
 * Holds static methods for calling the REST API functions.
 * These should be called asynchronously.
 */
public class APICalls {
    /**
	 * Performs the HTTP REST API request for a JSON listing of the
     * events nearest to the user's location.
     */
    public static Event[] getEventsNearLocation(Location location /*, Arguments */) {
        String getEventsUrl = "http://saypoint.dreamhosters.com/api/events/search/";
        RestClient client = new RestClient(getEventsUrl);
    	
        // Add parameters
        client.AddParam("latitude", ((Double)location.getLatitude()).toString());
        client.AddParam("longitude", ((Double)location.getLongitude()).toString());
	
    	try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Parse the given Events and return an ArrayList.
    	ArrayList<Event> events = new ArrayList<Event>();
    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            Iterator<?> keys = eventJSON.keys();

            while(keys.hasNext()) {
                String key = (String)keys.next();
                if (eventJSON.get(key) instanceof JSONObject) {
                    events.add(new Event(((JSONObject)eventJSON.get(key)).toString()));
                }
            }
        } catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing list of Events in getEventsNearLocation()");
            e.printStackTrace();
            return null;
        }

        return (Event[])events.toArray();
    }
    
    
    
    /**
     * Sends information to the REST API for creation of a new event.
     */
    public static boolean createEvent(Event newEvent) {
        String createEventUrl = "http://saypoint.dreamhosters.com/api/events";
        RestClient client = new RestClient(createEventUrl);

        client.AddParam("name", newEvent.getName());
        client.AddParam("description", newEvent.getDescription());
        //client.AddParam("type", newEvent.getType());
        client.AddParam("startTime", ((Long)newEvent.getStartTime()).toString());
        client.AddParam("end_date", ((Long)newEvent.getEndTime()).toString());
        LatLng location = newEvent.getLocation();
        client.AddParam("latitude", ((Double)location.latitude).toString());
        client.AddParam("longitude", ((Double)location.longitude).toString());

        try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //return new Event(client.getResponse());
        // We should return true or false if it worked or fail accordingly.
        boolean bool = false;
        JSONObject jsonObject;
        String name = null, name2 = null;
		try {
			jsonObject = new JSONObject(client.getResponse());
			name = jsonObject.get("text").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(APICalls.class.toString(), "Error Parsing Return Value from text");
			e.printStackTrace();
		}
		try {
			jsonObject = new JSONObject(client.getResponse());
			name2 = jsonObject.get("bool").toString();
		} catch (JSONException e) {
			Log.e(APICalls.class.toString(), "Error Parsing Return Value from bool");
			e.printStackTrace();
		}
		 
		if(name2 != null){
			bool = true;
		}
		if(name != null){
			bool = false;			
		}
        return bool; 
    }
}
