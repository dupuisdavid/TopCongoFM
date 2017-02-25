package com.dnd.radioTopCongo.toolbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {

	public DisplayUtils() {
		
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static int[] getSizes(Context mContext) {
		
	    int width = 0;
	    int height = 0;
	    
	    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	    Display display = (Display) wm.getDefaultDisplay();
	    
	    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
	    	
	        Point size = new Point();
	        display.getSize(size);
	        width = size.x;
	        height = size.y;
	        
	    } else {
	        width = display.getWidth();  // deprecated
	        width = display.getHeight();  // deprecated
	    }
	    
	    return new int[]{width, height};
	}

}
