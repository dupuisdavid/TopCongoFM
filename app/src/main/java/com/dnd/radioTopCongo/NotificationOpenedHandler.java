package com.dnd.radioTopCongo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.dnd.radioTopCongo.business.News;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import cz.msebera.android.httpclient.Header;

class NotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

    private static final String TAG = NotificationOpenedHandler.class.getSimpleName();

    private Context context;

    NotificationOpenedHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;

        if (data != null) {
            int newsId = data.optInt("newsId", 0);

            openMainActivity();
            managePushNotificationData(context, newsId);
        }
    }

    private void openMainActivity() {
        TopCongoApplication application = (TopCongoApplication) context.getApplicationContext();

        if (application != null) {
            MainPlayerActivity mainPlayerActivity = application.getMainPlayerActivity();

            if (mainPlayerActivity == null) {
                Log.i("PushReceiver", "Application has not been launched yet");
            } else {
                Log.i("PushReceiver", "Application has already been launched");
            }

            Intent mainPlayerIntent = new Intent(context, MainPlayerActivity.class);
            mainPlayerIntent.putExtra(Config.INTENT_EXTRA_KEY__START_PLAYING_RADIO, false);
            mainPlayerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(mainPlayerIntent);

            if (mainPlayerActivity != null) {
                mainPlayerActivity.pausePlayer();
            }
        }
    }

    private void managePushNotificationData(final Context context, int newsId) {
        Log.i(TAG, "newsId detected : " + newsId);

        try {
            if (newsId > 0) {
                getNewsDetailWithNewsId(context, String.valueOf(newsId), new GetNewsDetailCompletionRunnable() {
                    @Override
                    public void run() {}
                    @Override
                    public void run(News news) {
                        if (news != null) {
                            Log.i(TAG, "OPEN " + news.toString());
                            Intent intent = new Intent();
                            intent.setClassName("com.dnd.radioTopCongo", "com.dnd.radioTopCongo.NewsDetailActivity");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("News", news);
                            context.startActivity(intent);
                        }
                    }
                }, null);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    interface GetNewsDetailCompletionRunnable extends Runnable {
        void run(News news);
    }

    private void getNewsDetailWithNewsId(Context context, String newsId, final GetNewsDetailCompletionRunnable completionRunnable, final Runnable failureRunnable) {

        final TopCongoApplication application = (TopCongoApplication) context.getApplicationContext();
        Resources resources = application.getResources();

        String queryStringParameters = String.format(Locale.getDefault(), "?articleId=%s", newsId);
        String topCongoMobileDataApiDomain = resources.getString(R.string.topCongoMobileDataApiDomain);
        String topCongoMobileDataApiArticleServiceUrl = resources.getString(R.string.topCongoMobileDataApiArticleServiceUrl);

        String uri = String.format(topCongoMobileDataApiArticleServiceUrl, topCongoMobileDataApiDomain, queryStringParameters);
        Log.i(TAG, "REQUEST API URI: " + uri);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(10000);
        client.get(uri, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                if (response != null) {
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
            public void onFailure(int statusCode, Header[] headers, java.lang.String responseString, java.lang.Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (failureRunnable != null) {
                    failureRunnable.run();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }
}
