package com.dnd.radioTopCongo.toolbox;

import android.os.Handler;
import android.os.Looper;

public class HandlerUtilities extends Handler {

	public HandlerUtilities() {
		// TODO Auto-generated constructor stub
	}

	public HandlerUtilities(Callback callback) {
		super(callback);
		// TODO Auto-generated constructor stub
	}

	public HandlerUtilities(Looper looper) {
		super(looper);
		// TODO Auto-generated constructor stub
	}

	public HandlerUtilities(Looper looper, Callback callback) {
		super(looper, callback);
		// TODO Auto-generated constructor stub
	}

	
	public static Handler performRunnableAfterDelay(final Runnable runnable, int delay) {
		Handler handler = new Handler();
		handler.postDelayed(runnable, delay);
		
		return handler;
		
	}
}
