package com.appchallenge.eventspark;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/** Fragment containing a list of the events created previously by the user. */
public class MyEventsListFragment extends ListFragment {
	/** Reference to the parent activity. */
	private ActivityInterface mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
        	mActivity = (ActivityInterface)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ActivityInterface");
        }
    }

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_events_list, container, false);
    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

    	// Build the list of events.
        this.refreshList();

		// The list items can be long-clicked to bring up a context menu.
        registerForContextMenu(getListView());
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d("MyEventsListFragment.onListItemClick", "List item has been clicked.");

        Event event = (Event)l.getItemAtPosition(position);
        if (event == null)
        	return;
        
        mActivity.onEventSelected(event);
	}
    
    /** Refreshes the listing of created events from the local database. */
	public void refreshList() {
		// Get a list of the events we have created over time.
    	ArrayList<Event> allMyEvents = mActivity.getDatabase().getMyEvents();
    	if (allMyEvents.size() < 1) {
    		getView().findViewById(R.id.my_events_empty).setVisibility(View.VISIBLE);
    		getView().findViewById(android.R.id.list).setVisibility(View.GONE);
    		return;
    	}

    	getView().findViewById(R.id.my_events_empty).setVisibility(View.GONE);
    	getView().findViewById(android.R.id.list).setVisibility(View.VISIBLE);

    	// Partition the events into active and past lists.
    	ArrayList<Event> activeEvents = new ArrayList<Event>();
    	ArrayList<Event> pastEvents = new ArrayList<Event>();
    	for (Event e : allMyEvents) {
    		if (e.getEndDate().before(new Date()))
    			pastEvents.add(e);
    		else
    			activeEvents.add(e);
    	}

    	// Build the list adapter and sections.
		SeparatedListAdapter adapter = new SeparatedListAdapter(getActivity());
        if (activeEvents.size() > 0) {
		    EventAdapter active = new EventAdapter(getActivity(), R.layout.list_event, activeEvents);
			adapter.addSection(getResources().getString(R.string.active), active);
		}
        if (pastEvents.size() > 0) {
		    EventAdapter past = new EventAdapter(getActivity(), R.layout.list_event, pastEvents);
			adapter.addSection(getResources().getString(R.string.past), past);
		}
        setListAdapter(adapter);
	}
	
    // Container Activity must implement this interface.
    public interface ActivityInterface {
    	public LocalDatabase getDatabase();
        public void onEventSelected(Event event);
    }
}
