package com.appchallenge.android;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

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
    public static Event[] getEventsNearLocation(LatLng location) {
        String getEventsUrl = "http://saypoint.dreamhosters.com/api/events/search/";
        RestClient client = new RestClient(getEventsUrl);

        // Add parameters
        client.AddParam("latitude", ((Double)location.latitude).toString());
        client.AddParam("longitude", ((Double)location.longitude).toString());

    	try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.getEventsNearLocation", (client.getResponse() == null) ? "" : client.getResponse());

        // Parse the given Events and create an ArrayList.
    	ArrayList<Event> events = new ArrayList<Event>();
    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            
            if (!eventJSON.has("events")) {
            	Log.e("APICalls.getEventsNearLocation", "'events' key not present");
            	return null;
            }
            
            JSONArray eventsArray = eventJSON.getJSONArray("events");
            for (int i = 0; i < eventsArray.length(); ++i) {
            	JSONObject event = eventsArray.getJSONObject(i);
            	events.add(new Event(event.toString()));
            }
        } catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing list of Events in getEventsNearLocation()");
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
        	Log.e(APICalls.class.toString(), "Other parsing error - likely networking issue.");
        	e.printStackTrace();
            return null;
        }

    	Log.d("APICalls.getEventsNearLocation", "Found " + events.size() + " events.");

        return (Event[])events.toArray(new Event[events.size()]);
    }

    /**
     * Sends information to the REST API for creation of a new event.
     */
    public static Event createEvent(Event newEvent) {
    	String createEventUrl = "http://saypoint.dreamhosters.com/api/events";
        RestClient client = new RestClient(createEventUrl);

        client.AddParam("title", newEvent.getTitle());
        client.AddParam("description", newEvent.getDescription());
        client.AddParam("start_date", ((Long)newEvent.getStartTime()).toString());
        client.AddParam("end_date", ((Long)newEvent.getEndTime()).toString());
        LatLng location = newEvent.getLocation();
        client.AddParam("latitude", ((Double)location.latitude).toString());
        client.AddParam("longitude", ((Double)location.longitude).toString());

        try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String result = client.getResponse();
        Log.d("APICalls.createEvent", result);

        // Determine if an error has occurred.
        try {
			if ((new JSONObject(result)).has("error"))
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}

        return new Event(result);
    }
}
