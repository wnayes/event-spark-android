package com.appchallenge.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.appchallenge.android.Event.Type;

public class TypeFilterDialogFragment extends DialogFragment {
	/**
	 * The list of event types checked. Effectively the return value of this dialog.
	 */
	private ArrayList<Type> selectedTypes = new ArrayList<Type>();
	
	/**
	 * Interface allowing activities to receive the updated list of
	 * checked types.
	 */
	public interface TypeFilterDialogListener {
		public ArrayList<Type> receiveCurrentFilterList();
		public void onTypeFilterDialogOKClick(DialogFragment dialog, ArrayList<Type> selectedTypes);
	}
	
	/**
	 * The listener to dialog events; usually the parent activity.
	 */
	private TypeFilterDialogListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.filter_types_title);
	    
	    // Get the existing list of filtering types from the listener.
	    selectedTypes = mListener.receiveCurrentFilterList();
	    boolean[] checkboxValues = new boolean[Type.values().length];
	    for (Type type : selectedTypes) {
	    	// Convert from Type index values to list index values.
	    	int listIndex = type.getValue();
	    	listIndex = (listIndex == 0 ? Type.values().length - 1 : listIndex - 1);

	    	checkboxValues[listIndex] = true;
	    }

	    // Listener for checkbox events.
	    builder.setMultiChoiceItems(R.array.type_array, checkboxValues, new DialogInterface.OnMultiChoiceClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
	        	int typeIndex = (which == Type.values().length - 1 ? 0 : which + 1);
	        	Type clickedType = Type.typeIndices[typeIndex];
	            if (isChecked && !selectedTypes.contains(clickedType))
	            	selectedTypes.add(clickedType);
	            else if (!isChecked && selectedTypes.contains(clickedType))
	            	selectedTypes.remove(clickedType);
	        }
	    });

	    // Report the updated filtering list to the listener and exit.
	    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int id) {
	            mListener.onTypeFilterDialogOKClick(TypeFilterDialogFragment.this, selectedTypes);
	            dialog.dismiss();
	        }
	    });
	    
	    // The listener is not notified of any changes if cancel is chosen.
	    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int id) {
	        	dialog.dismiss();
	        }
	    });

	    return builder.create();
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // Attach the parent activity as a listener.
        try {
            mListener = (TypeFilterDialogListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TypeFilterDialogListener");
        }
    }
}
