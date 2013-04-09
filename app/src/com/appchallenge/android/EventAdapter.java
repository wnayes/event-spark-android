package com.appchallenge.android;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * ArrayAdapter for our Event objects that generates a specific list style.
 */
public class EventAdapter extends ArrayAdapter<Event> {

	private ArrayList<Event> items;
	private Context context;

	public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> events) {
		super(context, textViewResourceId, events);
		this.context = context;
		this.items = events;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_event, null);
        }

        Event item = items.get(position);
        if (item != null) {
            TextView title = (TextView)view.findViewById(R.id.list_event_title);
            if (title != null)
            	title.setText(item.getTitle());
            TextView description = (TextView)view.findViewById(R.id.list_event_description);
            if (description != null)
            	description.setText(item.getDescription());

            // Set the side bar color based on the event type.
            view.findViewById(R.id.list_event_colorbar).setBackgroundColor(item.getType().color());
        }

        return view;
    }
}
