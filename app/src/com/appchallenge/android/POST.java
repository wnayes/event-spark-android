package com.appchallenge.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class POST {

	private static StringBuilder sb = new StringBuilder();
	
	/**
	 * This is a simple working post method for java.
	 * It takes an string input and posts it.
	 * @param json_string
	 * @return
	 */
	public static String post(String json_string) {
		
		try {
            // Send data
            URL url = new URL("http://saypoint.dreamhosters.com/api/index.php/events");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json_string);
            wr.flush();

            // Get the response
            String line;
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            while ((line = rd.readLine()) != null) {
                sb.append(line + "\n");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
}
