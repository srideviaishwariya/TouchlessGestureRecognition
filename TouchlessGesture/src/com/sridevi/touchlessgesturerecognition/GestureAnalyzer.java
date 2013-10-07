package com.sridevi.touchlessgesturerecognition;

import android.util.Log;

public class GestureAnalyzer {
	private static final String TAG = "TouchlessGestureRecognition::GestureAnalyzer";
	private int SWIPE_MIN_DISTANCE = 200;
	private int SWIPE_THRESHOLD_VELOCITY = 10;
	
	public void setswipemaxdistance(int d){
		SWIPE_MIN_DISTANCE = d;
	}
	public void setswipethresholdvelocity(int d){
		SWIPE_THRESHOLD_VELOCITY = d;
	}
	public int compute(double x1, double x2){
		if(x1 - x2 > SWIPE_MIN_DISTANCE) {
			Log.i(TAG, "X1 = " + x1 + " X2 = " + x2);	
			Log.i(TAG, "LEFT SWIPE");		
		    return 1; 
		}else if(x2 - x1 > SWIPE_MIN_DISTANCE) {
			Log.i(TAG, "X1 = " + x1 + " X2 = " + x2);	
			Log.i(TAG, "RIGHT SWIPE");	
		    return 2; 
		}else
			return 0; 
	}	
}	