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
import java.util.Locale;

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

    public interface RequestFailureAction {
        void execute(Exception exception);
    }


    private boolean internetNetworkIsAvailable(Context context) {
        if (!Connectivity.networkReady(context)) {
            Toast.makeText(context, "Aucune connexion internet disponible.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private static final String ACTION_GET_PLAYLISTS = "playlists";

    private String buildBaseUri(String action) {
        return String.format(Locale.FRENCH, "https://www.googleapis.com/youtube/v3/%s", action);
    }

    public interface GetPlaylistsSuccessAction {
        void execute(ArrayList<YoutubePlaylist> items);
    }

    public AsyncHttpClient getPlaylists(@NonNull final Context context, @NonNull String apiKey, @NonNull String channelId, @NonNull final GetPlaylistsSuccessAction success, @NonNull final RequestFailureAction failure) {

        boolean internetNetworkIsAvailable = internetNetworkIsAvailable(context);
        if (!internetNetworkIsAvailable) {
            failure.execute(new Exception());
            return null;
        }

        String uri = String.format(Locale.FRENCH, "%s?key=%s&channelId=%s&part=contentDetails,id,localizations,player,snippet,status&maxResults=50", buildBaseUri(ACTION_GET_PLAYLISTS), apiKey, channelId);
        Log.i("uri", "" + uri);

        return HttpRestClient.get(context, uri, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.w(TAG, "YOUTUBE PLAYLISTS response: " + response);
                success.execute(null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                failure.execute(new Exception(throwable));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                failure.execute(new Exception(throwable));
            }
        });
    }
}

