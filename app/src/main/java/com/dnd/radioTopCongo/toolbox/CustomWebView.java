package com.dnd.radioTopCongo.toolbox;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CustomWebView extends WebView {

	public CustomWebView(Context context) {
		super(context);
	}
	
	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public int customGetContentHeight() {
        return this.getContentHeight();
    }

}
