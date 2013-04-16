package com.appchallenge.eventspark;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

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
    public static Event createEvent(Event newEvent, String userId, String userToken) {
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
        client.AddParam("user_type", ((Integer)newEvent.getUserType().getValue()).toString());
        if (userToken != null)
            client.AddParam("user_token", userToken);

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
		String getAttendUrl = "http://saypoint.dreamhosters.com/api/events/attend/" + id;
		RestClient client = new RestClient(getAttendUrl);

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

	public static String attendEvent(Integer eventId, String userId) {
		String attendUrl = "http://saypoint.dreamhosters.com/api/events/attend/" + eventId;
		RestClient client = new RestClient(attendUrl);

		client.AddParam("user_id", userId);

		try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.attendEvent", (client.getResponse() == null) ? "" : client.getResponse());

    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            if (eventJSON.has("error"))
				return null;
            if (eventJSON.has("result")) {
            	// Inform the UI whether we were able to attend.
            	return eventJSON.getString("result");
            }
            else {
            	Log.e("APICalls.attendEvent", "'result' key not present");
            	return null;
            }
    	} catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing JSON in attendEvent()");
            e.printStackTrace();
            return null;
        }
	}

	public static String unattendEvent(Integer eventId, String userId) {
		String unattendUrl = "http://saypoint.dreamhosters.com/api/events/attend/" + eventId;
		RestClient client = new RestClient(unattendUrl);

		client.AddParam("user_id", userId);

		try {
            client.Execute(RestClient.RequestMethod.DELETE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.unattendEvent", (client.getResponse() == null) ? "" : client.getResponse());

    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            if (eventJSON.has("error"))
				return null;
            if (eventJSON.has("result")) {
            	// Inform the UI whether we were able to unattend.
            	return eventJSON.getString("result");
            }
            else {
            	Log.e("APICalls.unattendEvent", "'result' key not present");
            	return null;
            }
    	} catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing JSON in unattendEvent()");
            e.printStackTrace();
            return null;
        }
	}

	public static String reportEvent(Integer eventId, Integer reasonCode, String userId) {
		String getReportUrl = "http://saypoint.dreamhosters.com/api/events/report/" + eventId;
		RestClient client = new RestClient(getReportUrl);

		client.AddParam("user_id", userId);
		client.AddParam("reason", reasonCode.toString());

		try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	Log.d("APICalls.reportEvent", (client.getResponse() == null) ? "" : client.getResponse());

    	try {
            JSONObject eventJSON = new JSONObject(client.getResponse());
            if (eventJSON.has("error"))
				return null;
            if (eventJSON.has("result")) {
            	// Inform the UI whether we were able to report.
            	return eventJSON.getString("result");
            }
            else {
            	Log.e("APICalls.attendEvent", "'result' key not present");
            	return null;
            }
    	} catch (JSONException e) {
            Log.e(APICalls.class.toString(), "Error parsing response in reportEvent()");
            e.printStackTrace();
            return null;
        }
	}

	/**
	 * Submits updated information about an event to the backend.
	 * @param existingEvent The event as it currently exists.
	 * @param updatedEvent The event with changes to members.
	 * @return A mapping from the mutable event member names to their new values.
	 */
	public static TreeMap<String, String> updateEvent(Event existingEvent, Event updatedEvent, String userId) {
    	String updateEventUrl = "http://saypoint.dreamhosters.com/api/events/" + existingEvent.getId();
        RestClient client = new RestClient(updateEventUrl);

        client.AddParam("user_id", userId);
        client.AddParam("secret_id", existingEvent.getSecretId());

        // Pass only the parameters that have been changed.
        if (!existingEvent.getTitle().equals(updatedEvent.getTitle()))
            client.AddParam("title", updatedEvent.getTitle());
        if (!existingEvent.getDescription().equals(updatedEvent.getDescription()))
            client.AddParam("description", updatedEvent.getDescription());
        if (!existingEvent.getType().equals(updatedEvent.getType()))
        	client.AddParam("type", ((Integer)updatedEvent.getType().getValue()).toString());
        if (!existingEvent.getStartDate().equals(updatedEvent.getStartDate()))
            client.AddParam("start_date", ((Long)(updatedEvent.getStartDate().getTime() / 1000)).toString());
        if (!existingEvent.getEndDate().equals(updatedEvent.getEndDate()))
            client.AddParam("end_date", ((Long)(updatedEvent.getEndDate().getTime() / 1000)).toString());

        // Ensure that we are passing at least one parameter besides the basic identifiers.
        if (client.getParamCount() < 3)
        	return null;

        try {
            client.Execute(RestClient.RequestMethod.PUT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("APICalls.updateEvent", (client.getResponse() == null) ? "" : client.getResponse());

        // Parse the updated members and return their values in a map.
        TreeMap<String, String> changes = new TreeMap<String, String>();
        try {
        	JSONObject changesJSON = new JSONObject(client.getResponse());
			if ((changesJSON).has("error")) {
				Log.e("APICalls.updateEvent", "An error has occured.");
				return null;
			}
			if (!(changesJSON).has("changes")) {
				Log.e("APICalls.updateEvent", "Unexpected JSON response.");
				return null;
			}

			JSONObject changedValues = changesJSON.getJSONObject("changes");
			if (changedValues.has("title"))
                changes.put("title", changedValues.getString("title"));
			if (changedValues.has("description"))
                changes.put("description", changedValues.getString("description"));
			if (changedValues.has("type"))
                changes.put("type", changedValues.getString("type"));
			if (changedValues.has("start_date"))
                changes.put("start_date", changedValues.getString("start_date"));
			if (changedValues.has("end_date"))
                changes.put("end_date", changedValues.getString("end_date"));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		} 
        
        if (changes.size() == 0)
        	return null;
        return changes;
	}
	
	public static Boolean deleteEvent(Event event, String userId) {
    	String deleteEventUrl = "http://saypoint.dreamhosters.com/api/events/" + event.getId();
        RestClient client = new RestClient(deleteEventUrl);

        client.AddParam("secret_id", event.getSecretId());
        client.AddParam("user_id", userId);

        try {
            client.Execute(RestClient.RequestMethod.DELETE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String result = client.getResponse();
        Log.d("APICalls.deleteEvent", result == null ? "" : result);

        // Determine if an error has occurred.
        try {
			if ((new JSONObject(result)).has("error")) {
				Log.e("APICalls.deleteEvent", "Error deleting event: " + (new JSONObject(result)).getString("error"));
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		} 

        return true;
	}

	// This is the API to share events with Facebook
	public static Boolean shareEvent(Integer id, String token) {
		String facebookURL = "https://graph.facebook.com/me/appchallenge_arrows:join";
		String eventURL = "http://saypoint.dreamhosters.com/facebook/" + id + ".html";
		Log.d("Just some checks", "in the API for share event");

		RestClient client = new RestClient(facebookURL);

        client.AddParam("event", eventURL);
        client.AddParam("access_token", token);
        
        try {
            client.Execute(RestClient.RequestMethod.POST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String result = client.getResponse();
        Log.d("APICalls.shareEvent", result == null ? "" : result);
        
        try {
			if ((new JSONObject(result)).has("error")) {
				Log.e("APICalls.shareEvent", "Error sharing event: " + (new JSONObject(result)).getString("error"));
				return false;
			}
			if ((new JSONObject(result)).has("id")) {
				Log.d("APICalls.shareEvent", "Success");
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		} 

		return false;
	}

	/**
	 * Determines if the device has internet connectivity.
	 * @return Whether a data connection is available.
	 */
	public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager =
          (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable());
    }

	/**
	 * Shows a message informing the user that an internet connection is not available.
	 */
	public static void displayConnectivityMessage(Context context) {
        Toast.makeText(context, "Please connect to the Internet and try again!", Toast.LENGTH_SHORT)
             .show();
	}
}
