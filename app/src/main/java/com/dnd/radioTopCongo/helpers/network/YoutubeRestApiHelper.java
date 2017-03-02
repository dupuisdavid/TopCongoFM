package com.dnd.radioTopCongo.helpers.network;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.purchase.InAppPurchase;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class YoutubeRestApiHelper {

    // Playlists: list
    // https://developers.google.com/youtube/v3/docs/playlists/list

    private static final String TAG = YoutubeRestApiHelper.class.getSimpleName();

    private static YoutubeRestApiHelper instance = new YoutubeRestApiHelper();

    public static YoutubeRestApiHelper getInstance() {
        if (instance == null) {
            instance = new YoutubeRestApiHelper();
        }
        return instance;
    }

    private YoutubeRestApiHelper() {}

    public interface RequestFailureRunnable extends Runnable {
        void run(Exception exception);
    }


    private boolean internetNetworkIsAvailable(Context context) {
        Resources resources = context.getResources();

        if (!Connectivity.networkReady(context)) {
            Toast.makeText(context, "Aucune connexion internet disponible.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public interface GetInAppPurchasesRequestSuccessRunnable extends Runnable {
        void run(ArrayList<InAppPurchase> items);
    }

    public AsyncHttpClient getInAppPurchases(@NonNull final Context context, @NonNull final GetInAppPurchasesRequestSuccessRunnable success, @NonNull final RequestFailureRunnable failure) {

        boolean internetNetworkIsAvailable = internetNetworkIsAvailable(context);
        if (!internetNetworkIsAvailable) {
            failure.run(new Exception());
            return null;
        }

        String urlString = "";
        Log.i("urlString", "" + urlString);

        return HttpRestClient.get(context, urlString, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Themes parsing
                String JSON_KEY_THEMES = "themes";
                ArrayList<InAppPurchase> inAppPurchaseThemes = null;

                if (inAppPurchaseThemes != null) {
                    success.run(inAppPurchaseThemes);
                } else {
                    failure.run(new Exception(""));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                failure.run(new Exception(throwable));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                failure.run(new Exception(throwable));
            }
        });
    }
}

