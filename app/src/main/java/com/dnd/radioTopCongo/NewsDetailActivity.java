package com.dnd.radioTopCongo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.dnd.radioTopCongo.business.News;
import com.dnd.radioTopCongo.helpers.GoogleApiHelper;
import com.dnd.radioTopCongo.toolbox.DisplayProperties;
import com.dnd.radioTopCongo.toolbox.HandlerUtilities;
import com.dnd.radioTopCongo.toolbox.Network;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusShare;
import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.ShortenedUrl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static com.rosaloves.bitlyj.Bitly.Provider;
import static com.rosaloves.bitlyj.Bitly.shorten;

public class NewsDetailActivity extends Activity {

	private static final String TAG = NewsDetailActivity.class.getSimpleName();
	
	private NewsDetailActivity activity = this;
	private FrameLayout wrapperView;
	private RelativeLayout innerWrapperView;
	private AdView adView;
	private RelativeLayout webViewWrapperLayout;
	private WebView webView;
	private News news;
	
	private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private CustomWebChromeClient customWebChromeClient;
    
    String urlToShare = "";
    
    // FACEBOOK
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private UiLifecycleHelper uiHelper;
    
    // TWITTER
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// Register your here app https://dev.twitter.com/apps/new and get your consumer key and secret
    // https://apps.twitter.com/app/7799560/keys

	static String TWITTER_CONSUMER_KEY = "vnsllPk8FIYgUVryfuGnn7CLV"; 									// place your consumer key here
	static String TWITTER_CONSUMER_SECRET = "vIGllHvSOcwQfE8dRe4d9VaXX3tMHkGYUkOw9NKtM7uvaA0kDz"; 		// place your consumer secret here
	static String TWITTER_OAUTH_ACCESS_TOKEN = "290529038-VKLU4KXNk3v5nTVKerWnKOLIBEV4IjNF2DMiuslI";
	static String TWITTER_OAUTH_ACCESS_TOKEN_SECRET = "qgTS7PYBbNlTrC2p5YQeiWfBchRZOnmZFT0A7b0Xa5kpw";

	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "com.dnd.radiotopcongo://twitterLoginSuccess";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	
	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;
	private ProgressDialog twitterProgressDialog;
	
	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_detail_activity);
		Log.i(TAG, "onCreate");
		
		// RETRIEVE DATA
		news = (News) getIntent().getSerializableExtra("News");
//		Log.i("Retrieve news", "" + news);
		
		wrapperView = (FrameLayout) findViewById(R.id.wrapperView);
		innerWrapperView = (RelativeLayout) findViewById(R.id.innerWrapperView);

		setupAdMobBanner();
		setupBackButton();
		setupWebView();

        Button privacyPolicyButton = (Button) findViewById(R.id.privacy_policy_button);
        if (privacyPolicyButton != null) {
            privacyPolicyButton.setPaintFlags(privacyPolicyButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            privacyPolicyButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NewsDetailActivity.this, PrivatePolicyActivity.class);
                    startActivity(intent);
                }
            });
        }
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
	    
	    // Shared Preferences
	 	mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
        
	}
	
	public void setupAdMobBanner() {
		
		// I keep getting the error 'The Google Play services resources were not found. 
		// Check your project configuration to ensure that the resources are included.'
		// https://developers.google.com/mobile-ads-sdk/kb/?hl=it#resourcesnotfound
		
		LayoutParams adViewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		adViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		adView = new AdView(this);
		adView.setLayoutParams(adViewLayoutParams);
	    adView.setAdUnitId(getResources().getString(R.string.googleAdMobNewsDetailBannerViewBlockIdentifier));
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
					NewsDetailActivity.this.finish();
					NewsDetailActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
				}
				
			});
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	public void setupWebView() {
		
		if (news == null || TextUtils.isEmpty(news.getContentURL())) {
            Log.e(TAG, "news is null: " + (news == null));
            Log.e(TAG, "news content URL is null or empty: " + (news != null ? TextUtils.isEmpty(news.getContentURL()) : "null"));
			return;
		}
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.clear();
		calendar.set(2011, Calendar.OCTOBER, 1);
		long secondsSinceEpoch = calendar.getTimeInMillis() / 1000L;
		
		
		// http://stackoverflow.com/questions/11773958/open-android-application-from-a-web-page
		
		String url = news.getContentURL();
		url = url + "?c=" + secondsSinceEpoch;
		Log.i("URL", url);
		
		
		webViewWrapperLayout = (RelativeLayout) findViewById(R.id.webViewWrapperLayout);
		webView = (WebView) findViewById(R.id.webView);
		customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
		
		WebSettings webSettings = webView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setSaveFormData(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setPluginState(WebSettings.PluginState.ON);
		
		webView.setFocusableInTouchMode(false);
		webView.setBackgroundColor(Color.WHITE);
		
		customWebChromeClient = new CustomWebChromeClient();
        webView.setWebChromeClient(customWebChromeClient);
		
		webView.addJavascriptInterface(new Object() {
			@JavascriptInterface 
		    public void getDocumentHeight(final int height) {
		        Log.i("getDocumentHeight", "height : " + height);
		        
		        runOnUiThread(new Runnable() {
		            @Override
		            public void run() {

		            	FrameLayout.LayoutParams webViewWrapperLayoutLayoutParams = (FrameLayout.LayoutParams) webViewWrapperLayout.getLayoutParams();
				        webViewWrapperLayoutLayoutParams.height = (int) ((float) (height + 90) * DisplayProperties.getInstance(activity).getPixelDensity());
				        webViewWrapperLayout.setLayoutParams(webViewWrapperLayoutLayoutParams);
				        webViewWrapperLayout.requestLayout();
				        
				        webView.invalidate();
				        webView.requestLayout();
				        
				        HandlerUtilities.performRunnableAfterDelay(new Runnable() {
							@Override
							public void run() {
								webView.setAlpha(1.0f);
							}
						}, 150);
				        
		            }
		        });
		        
		        
		    }
			
		}, "JsInterface");
		
		webView.setWebViewClient(new WebViewClient() {
			
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i(TAG, "shouldOverrideUrlLoading: " + url);
				
				String topCongoCustomUrl = "topcongo://";
				final Resources resources = activity.getResources();
				
				if (!url.startsWith(topCongoCustomUrl)) {
//					super.shouldOverrideUrlLoading(view, URL);				
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	                startActivity(intent);
	                
				} else {
					
					String[] separatedUrlFromCustomUrl = url.split(topCongoCustomUrl);
					String parametersString = separatedUrlFromCustomUrl[1];
					String[] separatedParametersFromSlash = parametersString.split("/");
					
					if (separatedParametersFromSlash.length > 0) {
						String methodNameString = separatedParametersFromSlash[0];
						
						ArrayList<HashMap<String, String>> parametersStrings = new ArrayList<>();
						
						for (int i = 1; i < separatedParametersFromSlash.length; i=i+2) {
							if ((i+1) < separatedParametersFromSlash.length) {
								HashMap<String, String> data = new HashMap<>();
								data.put("key", separatedParametersFromSlash[i]);
								data.put("value", separatedParametersFromSlash[i+1]);
								parametersStrings.add(data);
							}
							
						}
						
						if (parametersStrings.size() > 0) {
							
							Log.i("Parameters nb", "" + parametersStrings.size());
							
							for (int i = 0; i < parametersStrings.size(); i++) {
								Log.i("Parameters "+i, "" + parametersStrings.get(i).get("key") + " = " + parametersStrings.get(i).get("value"));
								if (parametersStrings.get(i).get("key").equals("url")) {
									urlToShare = parametersStrings.get(i).get("value");
									try {
										urlToShare = java.net.URLDecoder.decode(urlToShare, "UTF-8");
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
									}
								}
							}
							
							Log.i("urlToShare", "" + urlToShare);
							
							if (urlToShare.equals("")) {
                                if (!isFinishing()) {
                                    new AlertDialog.Builder(activity)
                                        .setTitle(resources.getString(R.string.appName))
                                        .setMessage(resources.getString(R.string.errorOccurs))
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {}

                                        })
                                        .show();
                                }

								
								return false;
							}

							if (methodNameString.equals("facebookArticleShare")) {
								Log.i(TAG, "facebookArticleShare");
								shareOnFacebook();				
							} else if (methodNameString.equals("twitterArticleShare")) {
								Log.i(TAG, "twitterArticleShare");
								shareOnTwitter();					
							} else if (methodNameString.equals("googlePlusArticleShare")) {
								Log.i(TAG, "googlePlusArticleShare");
								shareOnGooglePlus();
							} else if (methodNameString.equals("linkedInArticleShare")) {
								Log.i(TAG, "linkedInArticleShare");
								shareOnLinkedIn();
							}
							
						}
					}
				}
				
				return true;
				
            }
			 
			public void onPageFinished(WebView view, String URL) {
				webView.loadUrl("javascript:window.displayShareButtons();");
				webView.loadUrl("javascript:window.JsInterface.getDocumentHeight(Math.max(window.innerHeight, document.body.offsetHeight, document.documentElement.clientHeight));");
			}
			 
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {}
			
			@Override
		    public void onLoadResource(WebView view, String URL) {}
		});
		
		webView.loadUrl(url);
		
	}
	
	public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
    	customWebChromeClient.onHideCustomView();
    }
    
    class CustomWebChromeClient extends WebChromeClient {
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        	// To change body of overridden methods use File | Settings | File Templates.
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            // If a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            
            mCustomView = view;
            innerWrapperView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
            
            pauseRadioPlayer();
            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        @SuppressLint("InflateParams")
		@Override
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(NewsDetailActivity.this);
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();    //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null)
                return;

            innerWrapperView.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;
            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    
    public void pauseRadioPlayer() {
    	TopCongoApplication application = ((TopCongoApplication) getApplicationContext());
        
        if (application != null) {
        	MainPlayerActivity mainPlayerActivity = application.getMainPlayerActivity();
        	if (mainPlayerActivity != null) {
        		Toast.makeText(getApplicationContext(), "Radio en pause", Toast.LENGTH_SHORT).show();
        		mainPlayerActivity.pausePlayer();
        	}
        }
    }
    
    private void shareOnFacebook() {
    	
    	final Resources resources = getResources();
    	
    	// https://developers.facebook.com/docs/android/share?locale=fr_FR#linkshare-setup

		// Development Key Hashes command
		// keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
		// Release Key Hash command (production)
		// keytool -exportcert -alias topcongo -keystore /Users/DavidDupuis/Documents/Android/WorkspaceAndroid/RadioTopCongoKeyStore/topcongo-key-store | openssl sha1 -binary | openssl base64
		// keytool -exportcert -alias topcongo -keystore /Users/dupuisdavid/Documents/workspace/RadioTopCongoKeyStore/topcongo-key-store | openssl sha1 -binary | openssl base64
		
		try {
			
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(activity)
	        	.setLink(urlToShare)
	        	.build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		
		
		} catch (Exception e) {
			e.printStackTrace();

			if (!isFinishing()) {
				new AlertDialog.Builder(activity)
                    .setTitle(resources.getString(R.string.appName))
                    .setMessage(resources.getString(R.string.facebookApplicationRequiredToShareContent))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // https://play.google.com/store/apps/details?id=com.facebook.katana&hl=fr_FR

                            final String appPackageName = resources.getString(R.string.facebookApplicationPackageName);
                            try {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName + "&hl=fr_FR")));
                            }
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
			}

		}
    	
    }
    
    private void shareOnTwitter() {
    	
    	Boolean readyToTweet = true;
		
		if (!Network.networkIsAvailable(activity)) {
			readyToTweet = false;
		}
		
		if (TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
			readyToTweet = false;
		}
		
		// TODO
		// http://www.androidhive.info/2012/09/android-twitter-oauth-connect-tutorial/
		// http://stackoverflow.com/questions/17499935/android-twitter-integration-using-oauth-and-twitter4j
		// http://javatechig.com/android/how-to-integrate-twitter-in-android-application
		// https://github.com/itog/Twitter4j-android-Sample
		
		if (readyToTweet) {
			
			if (!isTwitterLoggedInAlready()) {
				loginToTwitter();
			} else {
				new updateTwitterStatus().execute("");
				
			}
		}
    }
    
    private void shareOnGooglePlus() {
    	
    	// Sharing to Google+ from your Android app
    	// https://developers.google.com/+/mobile/android/share/

		try {
            if (GoogleApiHelper.isGooglePlayServicesAvailable(this)) {
                Intent shareIntent = new PlusShare.Builder(this)
                        .setType("text/plain")
                        .setText(news.getTitle())
                        .setContentUrl(Uri.parse(urlToShare))
                        .getIntent();
                startActivityForResult(shareIntent, 0);
            }
		} catch (Exception e) {
			Toast.makeText(this, "Il semble que vous n'ayez pas installé l'application Google+ sur votre appareil. Partage impossible.", Toast.LENGTH_SHORT).show();
		}

    }
    
    private void shareOnLinkedIn() {
    	
    	// https://www.linkedin.com/secure/developer?showinfo=&app_id=3643433&acc_id=2060023&compnay_name=Top+Congo&app_name=Top+Congo+FM
    	// http://www.3pillarglobal.com/insights/part-2-using-socialauth-to-integrate-linkedin-api-in-android
    	
    }
    
    
    /**
	 * Function to login twitter
	 * */
	private void loginToTwitter() {
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			
			new Thread(new Runnable() { 
	            public void run(){        
	            	
	            	ConfigurationBuilder builder = new ConfigurationBuilder();
	    			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
	    			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
	    			
	    			Configuration configuration = builder.build();
	    			
	    			TwitterFactory factory = new TwitterFactory(configuration);
	    			twitter = factory.getInstance();

	    			try {
	    				
	    				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
	    				
	    				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
	    				
//	    				String url = Uri.parse(requestToken.getAuthenticationURL()).toString();
//	    				Log.i("url", "" + url);
//	    				
//	    				Intent intent = new Intent(NewsDetailActivity.this, TwitterLoginBrowserActivity.class);
//	    				intent.putExtra("url", url);
//	    				activity.startActivityForResult(intent, TWITTER_LOGIN_SUCCESS_REQUEST_CODE);
//	    				activity.overridePendingTransition(R.anim.activity_open_translate_y, R.anim.activity_close_scale);
	    				
	    				
	    			} catch (TwitterException e) {
	    				e.printStackTrace();
	    			}
	            	
	            }
	            
	        }).start();
			
		}
	}
	
    /**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}
    
	/**
	 * Function to update status
	 * */
	class updateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			twitterProgressDialog = new ProgressDialog(NewsDetailActivity.this);
			twitterProgressDialog.setMessage("Publication sur twitter en cours...");
			twitterProgressDialog.setIndeterminate(false);
			twitterProgressDialog.setCancelable(true);
			twitterProgressDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			
			Provider bitly = Bitly.as("o_urnip2na0", "R_3a70b94931ed03e5b859d39611415b3c");
	    	ShortenedUrl url = bitly.call(shorten(urlToShare));
			
			Log.d("Tweet Text", "> " + args[0]);
			String status = news.getTitle() +  "\n" + url.getShortUrl();
			Log.d("Tweet status", "> " + status);
			
			try {
				
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
				
				// Access Token 
				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
				
				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
				
				// Update status
				twitter4j.Status response = twitter.updateStatus(status);
				
				Log.d("Status", "> " + response.getText());
				
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			twitterProgressDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Tweet publié avec succès !", Toast.LENGTH_SHORT).show();
				}
			});
		}

	}
	
	/**
	 * Function to logout from twitter
	 * It will just clear the application shared preferences
	 * */
	@SuppressWarnings("unused")
	private void logoutFromTwitter() {
		// Clear the shared preferences
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();
		
		Toast.makeText(getApplicationContext(), "Déconnexion de Twitter effective !", Toast.LENGTH_SHORT).show();

	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (inCustomView()) {
                hideCustomView();
                return true;
            }

            if ((mCustomView == null) && (webView != null && webView.canGoBack())) {
                webView.goBack();
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		if (inCustomView()) {
            hideCustomView();
        } else if ((mCustomView == null) && (webView != null && webView.canGoBack())) {
            webView.goBack();
        } else {
        	super.onBackPressed();
    		NewsDetailActivity.this.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
		
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

		TopCongoApplication application = (TopCongoApplication) getApplication();
		Tracker tracker = application.getDefaultTracker();

		if (tracker != null) {
			tracker.setScreenName("NewsDetailView");
			tracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
    }

	@Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        
        if (adView != null) {
        	adView.resume();
        }
        
        if (webView != null) {
        	webView.onResume();
        }
        
        uiHelper.onResume();
    }
    
    private void manageTwitterCallBackUrl(Intent intent) {
    	
    	Uri uri = intent.getData();
    	
    	if (uri != null) {
    		
    		if (!isTwitterLoggedInAlready()) {
    			
    			if (uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
    				
    				Log.i("B1", "B1");
    				
    				// oAuth verifier
    				final String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

    				new Thread(new Runnable() { 
    		            public void run(){        
    		            	
    		            	try {
    							
    							// Get the access token
    							final AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

    							// Shared Preferences
    							Editor e = mSharedPreferences.edit();

    							// After getting access token, access token secret
    							// store them in application preferences
    							e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
    							e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
    							// Store login status - true
    							e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
    							e.commit(); // save changes

    							Log.e("Twitter OAuth Token", "" + accessToken.getToken());

    							// Getting user details from twitter
    							// For now i am getting his name only
    							long userID = accessToken.getUserId();
    							User user = twitter.showUser(userID);
    							@SuppressWarnings("unused")
    							final String username = user.getName();
    							
    							activity.runOnUiThread(new Runnable() {
    						        public void run() {
    						        	Log.i("B2", "B2");
    						        	new updateTwitterStatus().execute("");
    						        }
    						    });
    							
    							
    						} catch (Exception e) {
    							// Check log for login errors
    							Log.e("Twitter Login Error", "> " + e.getMessage());
    							e.printStackTrace();
    						}
    		            	
    		            }
    		            
    		        }).start();
    				
    			}
    			
    		} else {
    			
    			Log.i("B3", "B3");
    			new updateTwitterStatus().execute("");
    			
    		}
    	}

    	
    	
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
        
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        
        if (adView != null) {
        	adView.pause();
        }
        
        if (webView != null) {
        	webView.onPause();
        }
        
        uiHelper.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        
        if (inCustomView()) {
            hideCustomView();
        }
    }
    
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		Log.i(TAG, "onPostCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		
		if (adView != null) {
			adView.destroy();
		}
		
		uiHelper.onDestroy();
		
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });

	    
	}
	
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(null);
        Log.i(TAG, "onNewIntent");
        
        if (intent != null) {
        	Log.i("intent onNewIntent", "" + intent + ", flags : " + intent.getFlags() + ", action : " + intent.getAction() + ", scheme : " + intent.getScheme());
        	Bundle bundle = intent.getExtras();
        	Log.i("bundle onNewIntent", "" + bundle);
        	Uri uri = intent.getData();
        	String uriString = intent.getDataString();
        	Log.i("uriString onNewIntent", "" + uri + ", " + uriString);
        	
        	HandlerUtilities.performRunnableAfterDelay(new Runnable() {
				@Override
				public void run() {
					manageTwitterCallBackUrl(intent);
				}
			}, 1000);
        	
        }
    	
    }
	
}
