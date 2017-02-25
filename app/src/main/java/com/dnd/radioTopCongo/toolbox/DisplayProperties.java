package com.dnd.radioTopCongo.toolbox;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;

public class DisplayProperties {
	
	private static DisplayProperties displayPropertiesInstance;
	
	private float pixelDensity;
	private int densityDpi;
	private int widthPixels;
	private float xdpi;
	private float ydpi;
	private int deviceWidth;
	private int deviceHeight;
	
	public float getPixelDensity() {
		return pixelDensity;
	}

	public int getDensityDpi() {
		return densityDpi;
	}

	public int getWidthPixels() {
		return widthPixels;
	}

	public float getXdpi() {
		return xdpi;
	}

	public float getYdpi() {
		return ydpi;
	}

	public int getDeviceWidth() {
		return deviceWidth;
	}

	public int getDeviceHeight() {
		return deviceHeight;
	}

	public void setPixelDensity(float pixelDensity) {
		this.pixelDensity = pixelDensity;
	}

	public void setDensityDpi(int densityDpi) {
		this.densityDpi = densityDpi;
	}

	public void setWidthPixels(int widthPixels) {
		this.widthPixels = widthPixels;
	}

	public void setXdpi(float xdpi) {
		this.xdpi = xdpi;
	}

	public void setYdpi(float ydpi) {
		this.ydpi = ydpi;
	}

	public void setDeviceWidth(int deviceWidth) {
		this.deviceWidth = deviceWidth;
	}

	public void setDeviceHeight(int deviceHeight) {
		this.deviceHeight = deviceHeight;
	}

	public DisplayProperties(Activity activity) {
		
		DisplayMetrics display = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(display);
        int width = display.widthPixels;
        int height = display.heightPixels;
        
        this.setPixelDensity(display.density);
        this.setDensityDpi(display.densityDpi);
        this.setWidthPixels(display.widthPixels);
        this.setXdpi(display.xdpi);
        this.setYdpi(display.ydpi);
        this.setDeviceWidth(width);
        this.setDeviceHeight(height);
       
	}
	
	public static DisplayProperties getInstance(Activity activity) {
		if (displayPropertiesInstance == null) {
			displayPropertiesInstance = new DisplayProperties(activity);
		}
		
		return displayPropertiesInstance;
	}
	
	public void log() {
		// LOGS
        Log.i("DEVICE SCREEN RESOLUTION", getDeviceWidth() + "x" + getDeviceHeight());
        // LDPI : 0.75, MDPI : 1.0, HDPI : 1.5, XHDPI : 2.0, XXHDPI : 3.0, XXXHDPI : 4.0
        Log.i("DENSITY", "" +  getPixelDensity());
        // Density in terms of dpi
        Log.i("DENSITY_DPI", "" +  getDensityDpi());
        // Horizontal pixel resolution
        Log.i("WIDTH_PIXELS", "" +  getWidthPixels());
        Log.i("XDPI", "" +  getXdpi());
        Log.i("YDPI", "" +  getYdpi());
        Log.i("DEVICE SCREEN RESOLUTION", getDeviceWidth() + "x" + getDeviceHeight());
	}

}
