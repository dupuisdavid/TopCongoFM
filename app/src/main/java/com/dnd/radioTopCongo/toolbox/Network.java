package com.dnd.radioTopCongo.toolbox;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
	
	public static boolean networkIsAvailable(Context context) {
		Boolean connected = false;
		
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connectivityManager != null) {
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	        connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
		}
		
		return connected;
	}

}
