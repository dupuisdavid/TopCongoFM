package com.dnd.radioTopCongo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.dnd.radioTopCongo.business.News;
import com.dnd.radioTopCongo.toolbox.DisplayProperties;
import com.dnd.radioTopCongo.toolbox.NewsListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class NewsListActivity extends Activity {

    private NewsListActivity activity = this;
    private FrameLayout wrapperView;
    private AdView adView;

    private ArrayList<News> newsDataList;
    private ListView newsList;
    private TextView newsListLoadingStatusTextView;
    private NewsListAdapter newsListAdapter;

    private int currentPage = 0;
    private int totalPages = 0;
    private Boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("NewsListActivity", "onCreate");
        setContentView(R.layout.news_list_activity);

        wrapperView = (FrameLayout) findViewById(R.id.wrapperView);

        setupAdMobBanner();

        setupBackButton();
        setupNewsListView();
        getNewsList(true, new Runnable() {
            @Override
            public void run() {
//				newsListAdapter.setImageViewFadeRefreshEnable(false);
            }
        });

        Button privacyPolicyButton = (Button) findViewById(R.id.privacy_policy_button);
        if (privacyPolicyButton != null) {
            privacyPolicyButton.setPaintFlags(privacyPolicyButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            privacyPolicyButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NewsListActivity.this, PrivatePolicyActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    public void setupAdMobBanner() {

        // I keep getting the error 'The Google Play services resources were not found.
        // Check your project configuration to ensure that the resources are included.'
        // https://developers.google.com/mobile-ads-sdk/kb/?hl=it#resourcesnotfound

        LayoutParams adViewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        adViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        adView = new AdView(this);
        adView.setLayoutParams(adViewLayoutParams);
        adView.setAdUnitId(getResources().getString(R.string.googleAdMobNewsListBannerViewBlockIdentifier));
        adView.setAdSize(AdSize.BANNER);

        RelativeLayout adWrapperLayout = (RelativeLayout) findViewById(R.id.adWrapperLayout);
        adWrapperLayout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    public void setupBackButton() {

        ImageButton backButton = (ImageButton) wrapperView.findViewById(R.id.backButton);

        if (backButton != null) {
            backButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsListActivity.this.finish();
                    NewsListActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
                }

            });
        }

    }

    public void getNewsList(Boolean pagination, final Runnable endRequestRunnable) {
        requestForNewsList(pagination, new Runnable() {
            @Override
            public void run() {

                newsListAdapter.notifyDataSetChanged();

                if (newsList.getAlpha() == 0f) {
                    newsList.animate().alpha(1f).setDuration(350);
                    newsListLoadingStatusTextView.animate().alpha(0f).setDuration(350).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((RelativeLayout) newsListLoadingStatusTextView.getParent()).removeView(newsListLoadingStatusTextView);
                            newsListLoadingStatusTextView = null;
                        }
                    });
                }

                if (endRequestRunnable != null) {
                    endRequestRunnable.run();
                }

            }
        }, new Runnable() {
            @Override
            public void run() {
                setNewsListLoadingStatusTextViewMessage(getResources().getString(R.string.loadingError));

                if (endRequestRunnable != null) {
                    endRequestRunnable.run();
                }
            }
        });
    }

    public void setNewsListLoadingStatusTextViewMessage(String message) {
        if (newsListLoadingStatusTextView == null) {
            newsListLoadingStatusTextView = (TextView) findViewById(R.id.newsListLoadingStatusTextView);
        }

        if (newsListLoadingStatusTextView != null) {
            newsListLoadingStatusTextView.setText(message);
        }

    }

    private RelativeLayout hackFooterView;
    private RelativeLayout generateListFooterView() {
        int footerViewHeight = (int) (((float) 90) * DisplayProperties.getInstance(activity).getPixelDensity());

        AbsListView.LayoutParams footerViewlayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, footerViewHeight);
        final RelativeLayout footerView = new RelativeLayout(activity);
        footerView.setLayoutParams(footerViewlayoutParams);
        footerView.setBackgroundColor(0xffffffff);

        LayoutParams loadingTextViewlayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        TextView loadingTextView = new TextView(activity);
        loadingTextView.setLayoutParams(loadingTextViewlayoutParams);
        loadingTextView.setText("Chargement...");
        loadingTextView.setTextSize(12f);
        loadingTextView.setTextColor(0xff999999);
        loadingTextView.setGravity(Gravity.CENTER);

        footerView.addView(loadingTextView);

        return footerView;
    }

    private void destroyHackFooterView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            if (newsList.getFooterViewsCount() > 0 && hackFooterView != null) {
                newsList.removeFooterView(hackFooterView);
            }
        }
    }

    public void setupNewsListView() {

        setNewsListLoadingStatusTextViewMessage(getResources().getString(R.string.loadingProgress));

        newsDataList = new ArrayList<>();
        newsList = (ListView) findViewById(R.id.newsList);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            hackFooterView = generateListFooterView();
            newsList.addFooterView(hackFooterView);
        }

        newsListAdapter = new NewsListAdapter(this, newsDataList, false);
//      newsList.setLockScrollWhileRefreshing(true);
        newsList.setAdapter(newsListAdapter);

        newsList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true); // <== Will cause the highlight to remain

                News news = (newsDataList != null && newsDataList.size() > 0) &&
                        position < newsDataList.size() ? newsDataList.get(position) : null;

                if (news == null) {
                    return;
                }

                Intent intent = new Intent(activity, NewsDetailActivity.class);
                intent.putExtra("News", news);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

            }
        });

        // http://stackoverflow.com/questions/1080811/android-endless-list
        // http://stackoverflow.com/questions/12583419/android-listview-automatically-load-more-data-when-scroll-to-the-bottom
        // http://stackoverflow.com/questions/1080811/android-endless-list

        newsList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (newsList.getLastVisiblePosition() >= (newsList.getCount() - 1) && currentPage < (totalPages - 1)) {

                        if (!isLoading) {
                            Log.i("LOAD MORE", "LOAD MORE");

                            currentPage = currentPage + 1;
                            isLoading = true;

                            destroyHackFooterView();

                            final RelativeLayout footerView = generateListFooterView();
                            newsList.addFooterView(footerView);
                            newsListAdapter.setImageViewFadeRefreshEnable(false);
//				            newsList.setSelection(newsList.getAdapter().getCount() - 1);
                            newsList.smoothScrollToPosition((newsList.getAdapter().getCount() - 1));


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    getNewsList(true, new Runnable() {
                                        @Override
                                        public void run() {

                                            newsList.removeFooterView(footerView);
                                            newsListAdapter.setImageViewFadeRefreshEnable(true);
                                            newsListAdapter.notifyDataSetChanged();
                                            isLoading = false;

                                        }
                                    });

                                }
                            }, 550);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });

    }

    public void requestForNewsList(final Boolean pagination, final Runnable completionRunnable, final Runnable failureRunnable) {

        String queryStringParameters;

        if (pagination) {
            queryStringParameters = String.format(Locale.getDefault(), "?pageIndex=%d", currentPage);
        } else {
            queryStringParameters = String.format(Locale.getDefault(), "?requestNbArticles=%d", newsDataList.size());
        }

        String topCongoMobileDataAPIDomain = getResources().getString(R.string.topCongoMobileDataApiDomain);
        String topCongoMobileDataAPIArticlesServiceURL = getResources().getString(R.string.topCongoMobileDataApiArticlesServiceUrl);

        String URL = String.format(topCongoMobileDataAPIArticlesServiceURL, topCongoMobileDataAPIDomain, queryStringParameters);
        Log.i("URL", "" + URL);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(10000);
        client.get(URL, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {}

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {}

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i("JsonHttpClient", "onSuccess");

                if (response != null) {
                    try {

                        totalPages = response.getInt("totalPages");

                        JSONArray newsDataJsonDataList = (JSONArray) response.getJSONArray("articles");

                        if (newsDataJsonDataList.length() > 0) {

                            if (!pagination) {
                                newsDataList.clear();
                            }

                            for (int i = 0; i < newsDataJsonDataList.length(); i++) {
                                JSONObject newsJsonData = (JSONObject) newsDataJsonDataList.get(i);
                                News news = News.extractNewsDataFromJSONObject(newsJsonData);
                                newsDataList.add(news);
                            }
                        }

                        if (completionRunnable != null) {
                            completionRunnable.run();
                        }

                    } catch (JSONException e) {
                        Log.i("JSONException",  "" + e.toString());
                        if (failureRunnable != null) {
                            failureRunnable.run();
                        }

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
                Log.i("JsonHttpClient", "onFailure " + throwable + ", " + responseString);
                if (failureRunnable != null) {
                    failureRunnable.run();
                }

            }

            @Override
            public void onFinish() {
                Log.i("JsonHttpClient", "onFinish");
            }

        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NewsListActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);

    }

    @Override
    protected void onStart() {
        super.onStart();

        TopCongoApplication application = (TopCongoApplication) getApplication();
        Tracker tracker = application.getDefaultTracker();

        if (tracker != null) {
            tracker.setScreenName("NewsListView");
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i("NewsListActivity", "onPostCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

}
