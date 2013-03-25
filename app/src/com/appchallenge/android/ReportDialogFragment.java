package com.appchallenge.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ReportDialogFragment extends DialogFragment {
	/**
	 * Collection of the different reasons why an event might be reported.
	 */
	public static enum ReportReason {
		REASON_INACCURATE,
		REASON_OFFENSIVE,
		REASON_ILLEGAL
	}
	
	/**
	 * The reason that was last selected, defaulted to the first reason.
	 */
	private ReportReason selectedReason = ReportReason.REASON_INACCURATE;

	/**
	 * Interface allowing a parent activity to receive the report reason.
	 */
	public interface ReportDialogListener {
		public void onReportDialogOKClick(DialogFragment dialog, ReportReason reason);
	}
	
	/**
	 * The listener to dialog events; usually the parent activity.
	 */
	private ReportDialogListener mListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.event_report_title);

	    // Listener for radiobutton selection.
	    builder.setSingleChoiceItems(R.array.report_reasons, 0, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Update the selectedReason variable so we know which 
				selectedReason = ReportReason.values()[which];
			}
		});

	    // Notify the activity we wish to report the event.
	    builder.setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int id) {
	            mListener.onReportDialogOKClick(ReportDialogFragment.this, selectedReason);
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
            mListener = (ReportDialogListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ReportDialogListener");
        }
    }
}
