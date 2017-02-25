package com.dnd.radioTopCongo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.dnd.radioTopCongo.toolbox.AnimationUtilities;
import com.dnd.radioTopCongo.toolbox.HandlerUtilities;
import com.dnd.radioTopCongo.toolbox.Network;

public class TwitterLoginBrowserActivity extends Activity {

	private TwitterLoginBrowserActivity self = this;
	private FrameLayout rootView;
	private ProgressBar loadingProgressBar;
	private WebView webView;
	private String url;

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_login_browser_activity);
		
		rootView = (FrameLayout) findViewById(R.id.rootView);
		loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingProgressBar);
		webView = (WebView) rootView.findViewById(R.id.webView);
		
				
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		url = bundle.getString("url");
		
		if (Network.networkIsAvailable(self)) {
			
			if (url != null && !url.equals("")) {
				HandlerUtilities.performRunnableAfterDelay(new Runnable() {
					@Override
					public void run() {
						setupWebView();
					}
				}, 150);
			}
			
		}
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    Log.i("" + this.getClass().getSimpleName(), "onStart");
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	public void setupWebView() {
		Log.i("url", url);
		
		webView = (WebView) rootView.findViewById(R.id.webView);
		
		if (webView != null) {
			
			WebSettings webSettings = webView.getSettings();
			webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			webSettings.setSaveFormData(true);
			webSettings.setJavaScriptEnabled(true);
			webSettings.setLoadsImagesAutomatically(true);
			webSettings.setDomStorageEnabled(true);
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);
			webSettings.setBuiltInZoomControls(true);
			webSettings.setDisplayZoomControls(false);
			webSettings.setPluginState(WebSettings.PluginState.ON);
			
			webView.setFocusableInTouchMode(false);
			webView.setBackgroundColor(Color.WHITE);
		
			
			webView.setWebViewClient(new WebViewClient() {
				
				@Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
					super.shouldOverrideUrlLoading(view, url);
					
					if (!url.startsWith(TWITTER_CALLBACK_URL)) {				
						Log.i(this.getClass().getSimpleName().toString(), "" + TWITTER_CALLBACK_URL);
						
						Intent intent = new Intent();
                    	intent.putExtra("loginSuccess", true);
                    	
                    	self.setResult(RESULT_OK, intent);
                    	self.finish();
                    	self.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate_y);
						
		                return true;
					}
					
	                return false;
	            }
				 
				public void onPageFinished(WebView view, String url) {
					Log.i(this.getClass().getSimpleName().toString(), "onPageFinished");
					AnimationUtilities.fadeOut(loadingProgressBar, null);
					AnimationUtilities.fadeIn(webView, null);
				}
				 
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					Log.i(this.getClass().getSimpleName().toString(), "onReceivedError");
	            }
				
				@Override
			    public void onLoadResource(WebView view, String URL) {
//			        Log.i("onLoadResource", "URL : " + URL);
			    }
			});
			
			webView.loadUrl(url);
		}
		
		
	}
	
	public void onDetachedFromWindow(){
	    super.onDetachedFromWindow();
	    setVisible(false);
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate_y);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (webView != null) {
	    	webView.stopLoading();
	    	webView.setVisibility(View.GONE);
	    }
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

}
