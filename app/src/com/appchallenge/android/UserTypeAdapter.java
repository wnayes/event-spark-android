package com.appchallenge.android;

import java.util.ArrayList;

import com.appchallenge.android.Event.UserType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ArrayAdapter for our Event objects that generates a specific list style.
 */
public class UserTypeAdapter extends ArrayAdapter<UserType> {

	private ArrayList<UserType> types;
	private Context context;

	public UserTypeAdapter(Context context, int textViewResourceId, ArrayList<UserType> types) {
		super(context, textViewResourceId, types);
		this.context = context;
		this.types = types;
	}

	private View fillItemContent(View view, int position) {
		UserType type = types.get(position);
        if (type != null) {
            TextView text = (TextView)view.findViewById(R.id.item_usertype_text);
            ImageView icon = (ImageView)view.findViewById(R.id.item_usertype_icon);
            if (text != null && icon != null) {
            	// Set the image and text of the list item based on the user type.
            	if (type == UserType.ANONYMOUS) {
            		icon.setImageResource(R.drawable.person); // TODO: Anonymous icon.
            		text.setText("Post anonymously");
            	}
                else if (type == UserType.GPLUS) {
            		icon.setImageResource(R.drawable.gplus);
            		text.setText("Post using Google+ identity");
            	}
            	else if (type == UserType.FACEBOOK) {
            		icon.setImageResource(R.drawable.person); // TODO: Facebook Icon.
            		text.setText("Post using Facebook identity");
            	}
            }
        }
        return view;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_usertype, null);
        }

        return fillItemContent(view, position);
    }

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_usertype_dropdown, null);
        }

        return fillItemContent(view, position);
	}
}
