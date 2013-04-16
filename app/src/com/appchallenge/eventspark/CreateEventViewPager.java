package com.appchallenge.eventspark;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Special implementation of a ViewPager to remove the swipe
 * scrolling feature normally present.
 * See: http://stackoverflow.com/a/9650884/1168121
 */
public class CreateEventViewPager extends ViewPager {
    public CreateEventViewPager(Context context) {
        super(context);
    }

    public CreateEventViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        // Prevent swiping between pages. This lets the Google Map be
    	// fully in control.
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	// Prevent swiping between pages, signaling this handler took
    	// care of the touch event.
		return true;
    }
}
