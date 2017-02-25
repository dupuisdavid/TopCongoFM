package com.dnd.radioTopCongo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class PrivatePolicyActivity extends Activity {
	
	private PrivatePolicyActivity activity = this;
	private FrameLayout wrapperView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.private_policy_activity);
		
		// GET UI ELEMENTS
        wrapperView = (FrameLayout) findViewById(R.id.wrapperView);
        
        initCloseButton();
        initWebView();
	}
	
	@Override
    protected void onStart() {
        super.onStart();

		TopCongoApplication application = (TopCongoApplication) getApplication();
		Tracker tracker = application.getDefaultTracker();

		if (tracker != null) {
			tracker.setScreenName("PrivatePolicyView");
			tracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
    }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		PrivatePolicyActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
	}

	private void initCloseButton() {
		
		ImageButton closeButton = (ImageButton) wrapperView.findViewById(R.id.closeButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
				PrivatePolicyActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
			}
		});
		
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		
		Resources resources = getResources();
		
		String URL = resources.getString(R.string.private_policy_web_page_url);
		Log.i("URL", URL);

        WebView webView = (WebView) findViewById(R.id.webView);
		
		WebSettings webSettings = webView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setDomStorageEnabled(true);
		
		webView.setFocusableInTouchMode(false);
		webView.setBackgroundColor(Color.TRANSPARENT);  
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient() {
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String URL) {
				
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                startActivity(i);
				
                return true;
            }
			 
			public void onPageFinished(WebView view, String url) {

			}
			 
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                   
            }
			
		});

		webView.loadUrl(URL);
		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

}
