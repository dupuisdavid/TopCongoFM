package com.dnd.radioTopCongo.toolbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.dnd.radioTopCongo.MainPlayerActivity;
import com.dnd.radioTopCongo.R;
import com.dnd.radioTopCongo.TopCongoApplication;
import com.dnd.radioTopCongo.business.News;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class PushReceiver extends BroadcastReceiver {

	private static final String TAG = PushReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

	}

	public interface GetNewsDetailCompletionRunnable extends Runnable {
	    public void run(News news);
	}
	
	public void getNewsDetailWithNewsId(Context context, String newsId, final GetNewsDetailCompletionRunnable completionRunnable, final Runnable failureRunnable) {
		
		final TopCongoApplication application = (TopCongoApplication) context.getApplicationContext();
		Resources resources = application.getResources();
		
		String queryStringParameters = String.format(Locale.getDefault(), "?articleId=%s", newsId);
		String topCongoMobileDataAPIDomain = resources.getString(R.string.topCongoMobileDataApiDomain);
		String topCongoMobileDataAPIArticleServiceURL = resources.getString(R.string.topCongoMobileDataApiArticleServiceUrl);
		
		String url = String.format(topCongoMobileDataAPIArticleServiceURL, topCongoMobileDataAPIDomain, queryStringParameters);
		Log.i("URL", "" + url);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(10000);
		client.get(url, new JsonHttpResponseHandler() {
			@Override
            public void onStart() {}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {}
			
		    @Override
		    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
		    	super.onSuccess(statusCode, headers, response);
//		    	Log.i("JsonHttpClient", "onSuccess");
		    	
		    	if (response != null) {
//		    		Log.i("JSON", response.toString());

		    		News news = News.extractNewsDataFromJSONObject(response);
					
					if (completionRunnable != null) {
						completionRunnable.run(news);
					}
		    		
            	} else {
            		if (failureRunnable != null) {
            			failureRunnable.run();
		    		}
            	}
		    }
		    
		    @Override
		    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
		    	super.onFailure(statusCode, headers, responseString, throwable);
//		    	Log.i("JsonHttpClient", "onFailure " + throwable + ", " + responseString);
		    	if (failureRunnable != null) {
        			failureRunnable.run();
	    		}
		    	
            }

            @Override
            public void onFinish() {}

		});
	}
	
}


