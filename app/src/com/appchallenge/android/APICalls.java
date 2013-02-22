package com.appchallenge.android;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;
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
     * @throws ConnectTimeoutException 
     */
    public static ArrayList<Event> getEventsNearLocation(LatLng location) throws SocketTimeoutException, ConnectTimeoutException {
        String getEventsUrl = "http://saypoint.dreamhosters.com/api/events/search/";
        RestClient client = new RestClient(getEventsUrl);

        // Add parameters
        client.AddParam("latitude", ((Double)location.latitude).toString());
        client.AddParam("longitude", ((Double)location.longitude).toString());
        
        // Prevent the search from stalling indefinitely.
        client.SetTimeout(10000, 10000);

    	try {
            client.Execute(RestClient.RequestMethod.GET);
    	} catch (SocketTimeoutException sce) {
    		Log.e("APICalls.getEventsNearLocation", "Socket connection timeout.");
    		throw sce;
    	} catch (ConnectTimeoutException cte) {
        	Log.e("APICalls.getEventsNearLocation", "HTTP Connection timeout.");
        	throw cte;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        return events;
    }
    
    /**
	 * Retrieves an Event from its ID.
     */
    public static Event getEvent(int id) {
        String getEventUrl = "http://saypoint.dreamhosters.com/api/events/" + id;
        RestClient client = new RestClient(getEventUrl);

    	try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.getEvent", (client.getResponse() == null) ? "" : client.getResponse());

        // Parse the newly received Event.
    	Event event;
    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());

            if (!eventJSON.has("event")) {
            	Log.e("APICalls.getEvent", "'event' key not present");
            	return null;
            }

            if (eventJSON.has("error")) {
            	Log.e("APICalls.getEvent", "Error: " + eventJSON.getString("error"));
                return null;
            }

            // Create a new Event object directly from the given JSON.
            event = new Event(eventJSON.toString());
        } catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing Event in getEvent()");
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
        	Log.e(APICalls.class.toString(), "Other parsing error - likely networking issue.");
        	e.printStackTrace();
            return null;
        }

        return event;
    }

    /**
     * Sends information to the REST API for creation of a new event.
     */
    public static Event createEvent(Event newEvent, String userId) {
    	String createEventUrl = "http://saypoint.dreamhosters.com/api/events";
        RestClient client = new RestClient(createEventUrl);

        client.AddParam("title", newEvent.getTitle());
        client.AddParam("description", newEvent.getDescription());
        client.AddParam("start_date", ((Long)(newEvent.getStartDate().getTime() / 1000)).toString());
        client.AddParam("end_date", ((Long)(newEvent.getEndDate().getTime() / 1000)).toString());
        client.AddParam("type", ((Integer)newEvent.getType().getValue()).toString());
        LatLng location = newEvent.getLocation();
        client.AddParam("latitude", ((Double)location.latitude).toString());
        client.AddParam("longitude", ((Double)location.longitude).toString());
        client.AddParam("user_id", userId);

        try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String result = client.getResponse();
        Log.d("APICalls.createEvent", result == null ? "" : result);

        // Determine if an error has occurred.
        try {
			if ((new JSONObject(result)).has("error"))
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		} 

        return new Event(result);
    }

	public static int getAttendance(int id) {
		String getAttendUrl = "http://saypoint.dreamhosters.com/api/events/getAttend/";
		RestClient client = new RestClient(getAttendUrl);

		client.AddParam("id", Integer.toString(id));

		try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.getAttendance", (client.getResponse() == null) ? "" : client.getResponse());

    	int attending = 0;
    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            attending = Integer.parseInt((eventJSON.get("attending").toString()));
            
            if (!eventJSON.has("attending")) {
            	Log.e("APICalls.getAttendance", "'attending' key not present");
            	return 0;
            }

    	} catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing JSON in getAttendance()");
            e.printStackTrace();
            return 0;
        }

    	return attending;
	}

	public static int joinAttendance(int id) {
		String getAttendUrl = "http://saypoint.dreamhosters.com/api/events/attend/";
		RestClient client = new RestClient(getAttendUrl);

		client.AddParam("id", Integer.toString(id));

		try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.joinAttendance", (client.getResponse() == null) ? "" : client.getResponse());

    	int attending = 0;
    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());            
            if (!eventJSON.has("text")) {
            	Log.e("APICalls.joinAttendance", "'text' key not present");
            	return 0;
            }

            // TODO: Receive updated attendance value.
            attending = 1;

    	} catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing JSON in joinAttendance()");
            e.printStackTrace();
            return 0;
        }

    	return attending;
	}

	public static int report(int id) {
		String getAttendUrl = "http://saypoint.dreamhosters.com/api/events/report/";
		RestClient client = new RestClient(getAttendUrl);

		client.AddParam("id", Integer.toString(id));

		try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.report", (client.getResponse() == null) ? "" : client.getResponse());

    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            if (!eventJSON.has("text")) {
            	Log.e("APICalls.report", "'text' key not present");
            	return 0;
            }
    	} catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing list of Events in report()");
            e.printStackTrace();
            return 0;
        }

    	return 1;
	}
}
