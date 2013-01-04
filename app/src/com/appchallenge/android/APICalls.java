package com.appchallenge.android;

import android.location.Location;

/**
 * Holds static methods for calling the REST API functions.
 * These should be called asynchronously.
 */
public class APICalls {
	/**
	 * Performs the HTTP REST API request for a JSON listing of the
     * events nearest to the user's location.
	 */
    public static String getEventsNearLocation(Location location /*, Arguments */) {
    	String getEventsUrl = "http://www.OUR-SERVER-API-URL.com/";
    	RestClient client = new RestClient(getEventsUrl);
    	
    	// Add parameters
    	
    	try {
			client.Execute(RestClient.RequestMethod.GET);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return client.getResponse();
    }
    
    /**
     * Sends information to the REST API for creation of a new event.
     */
    public static String createEvent(/* Arguments */) {
    	String createEventUrl = "http://www.OUR-SERVER-API-URL.com/";
    	RestClient client = new RestClient(createEventUrl);
    	
    	// Add parameters
    	
    	try {
			client.Execute(RestClient.RequestMethod.POST);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return client.getResponse();
    }
}
