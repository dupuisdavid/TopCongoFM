package com.dnd.radioTopCongo.helpers.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpRestClient {

	private static int HttpDefaultTimeoutDuration = 10000;

	public static AsyncHttpClient get(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		return HttpRestClient.get(context, url, HttpDefaultTimeoutDuration, params, responseHandler);
	}
	
	public static AsyncHttpClient get(Context context, String url, int timeoutDuration, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		AsyncHttpClient client = new AsyncHttpClient();
		
		client.setTimeout(timeoutDuration);
		client.setURLEncodingEnabled(false);
		client.get(context, url, params, responseHandler);

		return client;
	}

	public static AsyncHttpClient post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		AsyncHttpClient client = new AsyncHttpClient();
		
		client.setTimeout(HttpDefaultTimeoutDuration);
		client.setURLEncodingEnabled(false);
		client.post(context, url, params, responseHandler);
		
		return client;
	}

}
