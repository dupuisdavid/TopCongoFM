package com.dnd.radioTopCongo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.ContentObserver;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dnd.radioTopCongo.business.News;
import com.dnd.radioTopCongo.helpers.network.YoutubePlaylist;
import com.dnd.radioTopCongo.helpers.network.YoutubeRestApiHelper;
import com.dnd.radioTopCongo.toolbox.DisplayProperties;
import com.dnd.radioTopCongo.toolbox.Network;
import com.dnd.radioTopCongo.toolbox.NewsListAdapter;
import com.facebook.AppEventsLogger;
import com.google.android.gms.ads.purchase.InAppPurchase;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainPlayerActivity extends Activity 
	implements 
	OnPreparedListener, 
	OnBufferingUpdateListener, 
	OnCompletionListener, 
	OnInfoListener, 
	OnErrorListener {

    private static final String TAG = MainPlayerActivity.class.getSimpleName();
	
	private MainPlayerActivity activity = this;
	private FrameLayout wrapperView;
	private ImageButton playPauseButton;
	private SeekBar volumeSeekBar;
    private TextView playerStatusTextView;
    private MediaPlayer mediaPlayer;
	private String STREAM_URI;
	
	
	private ArrayList<News> newsDataList;
	private ListView focusNewsList;
	private TextView focusNewsListLoadingStatusTextView;
	private NewsListAdapter focusNewsListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i("MainPlayerActivity", "onCreate");
		setContentView(R.layout.main_player_activity);
		
		STREAM_URI = getResources().getString(R.string.radioTopCongoStreamURL);
		
		Intent intent = getIntent();
        boolean startPlayingRadio = intent.getBooleanExtra(Config.INTENT_EXTRA_KEY__START_PLAYING_RADIO, true);
		
		// Solution against MediaPlayer verbose logging
		// http://stackoverflow.com/questions/7889888/how-to-filter-out-a-tagname-in-eclipse-logcat-viewer
		// ^(?!.*(MediaPlayer)).*$
		
		// http://srblog.info/?page_id=281
		// http://stackoverflow.com/questions/10641793/android-how-to-display-the-buffering-from-onbufferingupdate (onBufferingUpdate (-2147483648))
		// https://github.com/TheBiggerGuy/FreshAir
		// https://github.com/TheBiggerGuy/FreshAir/blob/master/Android/Workspace/FreshAirRadio_10/src/uk/org/freshair/android/FreshAirRadio.java
		// http://stackoverflow.com/questions/4180670/android-mediaplayer-is-preparing-too-long
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
//		printDisplayProperties();
        setupPlayerViews();
        
        Log.i("startPlayingRadio", "" + startPlayingRadio);
        
        if (startPlayingRadio) {
        	startPlaying();
        } else {
        	setPlayerInterfaceToPauseOrStopMode(activity.getResources().getString(R.string.radioTopCongoPressPlayToStartListening));
        }

        Button privacyPolicyButton = (Button) findViewById(R.id.privacy_policy_button);
        if (privacyPolicyButton != null) {
            privacyPolicyButton.setPaintFlags(privacyPolicyButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            privacyPolicyButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainPlayerActivity.this, PrivatePolicyActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        
        setupAllNewsButton();
        setupNewsListView();
		getNewsList(new Runnable() {
			@Override
			public void run() {
//				focusNewsListAdapter.setImageViewFadeRefreshEnable(false);
			}
		});
		
		TopCongoApplication application = ((TopCongoApplication) getApplicationContext());
		application.setMainPlayerActivity(this);


		// https://developers.google.com/apis-explorer/#p/youtube/v3/

        String apiKey = "AIzaSyBrojvIsA1RpplsA_UeWvzBnpWkaFZ8wC0";
        String channelId = "UCiZ-kZv-UfgXMClPl98BZzg";
/*
		YoutubeRestApiHelper.getInstance().getPlaylists(this, apiKey, channelId, new YoutubeRestApiHelper.GetPlaylistsSuccessAction() {
			@Override
			public void execute(ArrayList<YoutubePlaylist> items) {

			}

		}, new YoutubeRestApiHelper.RequestFailureAction() {
			@Override
			public void execute(Exception exception) {

			}
		});
*/


/*
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential()
                .setAccessToken(apiKey);
        YouTube youtube = new YouTube.Builder(transport, jsonFactory, credential).setApplicationName("TOP CONGO FM").build();

        try {
            YouTube.Search.List query = youtube.search().list("id,snippet");
            query.setKey(apiKey);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");


            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result:results) {
                Log.d(TAG, "RESULT: " + result.getSnippet().getTitle());
            }


        } catch(IOException e) {
            Log.d("YC", "Could not initialize: "+e);
        }
*/

		
//		printKeyHash();
	}
	
	@SuppressWarnings("unused")
	private void printKeyHash(){
	    // Add code to print out the key hash
	    try {
	        PackageInfo info = getPackageManager().getPackageInfo("com.dnd.radioTopCongo", PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	        }
	    } catch (NameNotFoundException e) {
	        Log.d(TAG, "NameNotFoundException: " + e.toString());
	    } catch (NoSuchAlgorithmException e) {
	        Log.d(TAG, "NoSuchAlgorithmException: " + e.toString());
	    }
	}
	
	public void getNewsList(final Runnable endRequestRunnable) {
		requestForNewsList(new Runnable() {
			@Override
			public void run() {
				
				focusNewsListAdapter.notifyDataSetChanged();
				
				if (focusNewsList.getAlpha() == 0f) {
					focusNewsList.animate().alpha(1f).setDuration(350);
					focusNewsListLoadingStatusTextView.animate().alpha(0f).setDuration(350).setListener(new AnimatorListenerAdapter() {
			            @Override
			            public void onAnimationEnd(Animator animation) {
			            	((RelativeLayout) focusNewsListLoadingStatusTextView.getParent()).removeView(focusNewsListLoadingStatusTextView);
			            	focusNewsListLoadingStatusTextView = null;
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
				setFocusNewsListLoadingStatusTextViewMessage(getResources().getString(R.string.loadingError));
				
				if (endRequestRunnable != null) {
					endRequestRunnable.run();
				}
			}
		});
	}
	
	public void printDisplayProperties() {
		DisplayProperties.getInstance(this).log();
	}
	
	public void setupPlayerViews() {
		
		// GET UI ELEMENTS
        
        wrapperView = (FrameLayout) findViewById(R.id.wrapperView);
        
        ImageButton contactButton = (ImageButton) wrapperView.findViewById(R.id.contactButton);
		contactButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, ContactActivity.class);
            	startActivity(intent);
                overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
			}
		});
		
		ImageButton informationButton = (ImageButton) wrapperView.findViewById(R.id.informationButton);
		informationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, InformationActivity.class);
            	startActivity(intent);
                overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
			}
		});
        
        playPauseButton = (ImageButton) wrapperView.findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mediaPlayer != null) {
					
					try {		            	
						if (mediaPlayer.isPlaying()) {
							pausePlayer();
						} else {
							startPlaying();
						}	            
		            } catch (IllegalStateException e) {
		                e.printStackTrace();
		            }
				} else {
					startPlaying();
				}
				
			}
			
		});
        
		int currentVolume = getAudioStreamVolume();
//      Log.i("currentVolume", "" + currentVolume);
		
        // VOLUME SEEK BAR
        volumeSeekBar = (SeekBar) wrapperView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {				
				
				float volume = (float) (progress/100.0);
//				Log.i("Volume", "" + volume);
				
				try {
//					Log.i("mediaPlayer", "" + (mediaPlayer != null));
					
					if (mediaPlayer != null) {
						mediaPlayer.setVolume(volume, volume);
					}

				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
				
			}
		});
               
        // VOLUME OBSERVER
        SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
        
        playerStatusTextView = (TextView) findViewById(R.id.playerStatusTextView);
	}
	
	public void setFocusNewsListLoadingStatusTextViewMessage(String message) {
		if (focusNewsListLoadingStatusTextView == null) {
			focusNewsListLoadingStatusTextView = (TextView) findViewById(R.id.focusNewsListLoadingStatusTextView);
		}
		
		if (focusNewsListLoadingStatusTextView != null) {
			focusNewsListLoadingStatusTextView.setText(message);
		}
		
	}
	
	public void setupAllNewsButton() {
		
		Button allNewsImageButton = (Button) wrapperView.findViewById(R.id.allNewsButton);
		allNewsImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(activity, NewsListActivity.class);
                startActivity(intent);
            	overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
            	
			}
			
		});
        
	}
	
	public void setupNewsListView() {
		
		setFocusNewsListLoadingStatusTextViewMessage(getResources().getString(R.string.loadingProgress));
		
		
		newsDataList = new ArrayList<>();
		
		focusNewsList = (ListView) findViewById(R.id.focusNewsList);
        focusNewsListAdapter = new NewsListAdapter(this, newsDataList, true);
 
//      focusNewsList.setLockScrollWhileRefreshing(true);
        focusNewsList.setAdapter(focusNewsListAdapter);
        focusNewsList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   
                view.setSelected(true); // <== Will cause the highlight to remain
                
                News news = newsDataList.get(position);

                Intent intent = new Intent(activity, NewsDetailActivity.class);
                intent.putExtra("News", news);
                startActivity(intent);
            	overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                
            }
        });

	}
	
	public void requestForNewsList(final Runnable completionRunnable, final Runnable failureRunnable) {
		
		int playerViewArticlesRequestNb = Integer.parseInt(getResources().getString(R.string.playerViewArticlesRequestNb));
		String queryStringParameters = String.format(Locale.getDefault(), "?requestNbArticles=%d", playerViewArticlesRequestNb);

		
		String topCongoMobileDataAPIDomain = getResources().getString(R.string.topCongoMobileDataApiDomain);
		String topCongoMobileDataAPIArticlesServiceURL = getResources().getString(R.string.topCongoMobileDataApiArticlesServiceUrl);
		
		String URL = String.format(topCongoMobileDataAPIArticlesServiceURL, topCongoMobileDataAPIDomain, queryStringParameters);
		Log.i("URL", "" + URL);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(10000);
		client.get(URL, new JsonHttpResponseHandler() {
			@Override
            public void onStart() {
				
            }
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

            }
			
		    @Override
		    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
		    	super.onSuccess(statusCode, headers, response);
		    	Log.i("JsonHttpClient", "onSuccess");
		    	
		    	if (response != null) {
//		    		Log.i("JSON", response.toString());

		    		try {
		    			
		    			JSONArray newsDataJsonDataList = response.getJSONArray("articles");
		    			
		    			if (newsDataJsonDataList.length() > 0) {
		    				
		    				newsDataList.clear();
		    				
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
    protected void onStart() {
        super.onStart();
//      Log.i("MainPlayerActivity", "onStart");

		TopCongoApplication application = (TopCongoApplication) getApplication();
		Tracker tracker = application.getDefaultTracker();

		if (tracker != null) {
			tracker.setScreenName("PlayerView");
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
        AppEventsLogger.activateApp(this);
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyMediaPlayer();
    }
	
	
	public void startPlaying() {
        
        if (mediaPlayer == null) {
        	
            try {
            	
            	if (!Network.networkIsAvailable(activity)) {
            		onError(mediaPlayer, -1, -1);
	        	}
            	
            	Log.i("MediaPlayer", "Initialization");

            	mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                
                mediaPlayer.setOnBufferingUpdateListener(activity);
                mediaPlayer.setOnCompletionListener(activity);
                mediaPlayer.setOnErrorListener(activity);
                mediaPlayer.setOnPreparedListener(activity);
                mediaPlayer.setOnInfoListener(activity);
                
                mediaPlayer.setDataSource(STREAM_URI);
                mediaPlayer.prepareAsync();
                
                
                setPlayerInterfaceToPlayMode(getResources().getString(R.string.radioTopCongoBuffering));
                
                
            } catch (IllegalArgumentException e) {
                onError(mediaPlayer, -1, -1);
            } catch (IllegalStateException e) {
                onError(mediaPlayer, -1, -1);
            } catch (IOException e) {
                onError(mediaPlayer, -1, -1);
            }     

        } else {
        	try {
        		if (mediaPlayer.isPlaying()) {
                	return;
                }
        		
        		Log.i("MediaPlayer", "Play");
            	mediaPlayer.start();
            	setPlayerInterfaceToPlayMode();
                
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return;
            }
        }
        
        
    }
	
	public void setPlayerInterfaceToPauseOrStopMode(String message) {
		playerStatusTextView.setText(message);
        playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
	}
	
	public void setPlayerInterfaceToPlayMode() {
		setPlayerInterfaceToPlayMode(getResources().getString(R.string.radioTopCongoOnAir));
	}
	
	public void setPlayerInterfaceToPlayMode(String message) {
		playerStatusTextView.setText(message);
    	playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.pause));
	}
	
	public void pausePlayer() {
		Log.i("MediaPlayer", "Pause");
		
		if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
			return;
		}
		
		mediaPlayer.pause();
		playerStatusTextView.setText(getResources().getString(R.string.radioTopCongoPause));
		playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
		
	}

	@Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.i("MediaPlayer", "onInfo (" + what + " - " + extra + ")");
        
//      1 : MEDIA_INFO_UNKNOWN
//      MEDIA_INFO_VIDEO_TRACK_LAGGING
//      MEDIA_INFO_VIDEO_RENDERING_START
//      MEDIA_INFO_BUFFERING_START
//      MEDIA_INFO_BUFFERING_END
//      MEDIA_INFO_BAD_INTERLEAVING
//      MEDIA_INFO_NOT_SEEKABLE
//      MEDIA_INFO_METADATA_UPDATE
//      MEDIA_INFO_UNSUPPORTED_SUBTITLE
//      MEDIA_INFO_SUBTITLE_TIMED_OUT
        
        if (!Network.networkIsAvailable(activity)) {
    		onError(mediaPlayer, -1, -1);
    	}

        
        switch (what) {
        
        	// MediaPlayer.MEDIA_INFO_UNKNOWN
        	case 1:
        		
        		break;
	        // MediaPlayer.MEDIA_INFO_BUFFERING_START
	        case 701:
	        	playerStatusTextView.setText(getResources().getString(R.string.radioTopCongoBuffering));
	            break;   
	        // MediaPlayer.MEDIA_INFO_BUFFERING_END
	        case 702: 
	        	setPlayerInterfaceToPlayMode();
	            break;
	        // MediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH
	        case 703: 
	        	
	            break;
	        // MediaPlayer.MEDIA_INFO_METADATA_UPDATE
	        case 802: 
	        	
	        	break;
	        default:
	            break;
        }

        return true;
    }
	
	
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("MediaPlayer", "onError (" + what + ", "+ extra +")");

        // http://developer.android.com/reference/android/media/MediaPlayer.OnErrorListener.html

//      MEDIA_ERROR_IO
//      MEDIA_ERROR_MALFORMED
//      MEDIA_ERROR_UNSUPPORTED
//      MEDIA_ERROR_TIMED_OUT   

        switch (extra) {
        	// MEDIA_ERROR_IO
        	case -1004:
        		
        		break;
	        // MEDIA_ERROR_MALFORMED
	        case -1007:
	        	
	            break;
	        // MEDIA_ERROR_UNSUPPORTED
	        case -1010:
	        	
	            break;
	        // MEDIA_ERROR_TIMED_OUT
	        case -110:
	        	
	            break;
	        default:
	            break;
        }
        
        setPlayerInterfaceToPauseOrStopMode(activity.getResources().getString(R.string.radioTopCongoConnectionLost));
        destroyMediaPlayer();

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    	Log.i("MediaPlayer", "onPrepared");
    	
    	try {
        	mediaPlayer.start();
        } catch (IllegalStateException e) {
            onError(mp, -1, -1);
        }
        
    	setPlayerInterfaceToPlayMode();
    	
    }
    
    @Override
    public void onCompletion(MediaPlayer mp) {
    	Log.i("MediaPlayer", "onCompletion");
    	
    	if (mp == null) {
        	return;
        }
            
    	destroyMediaPlayer();
        startPlaying();
        
    }

    @Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
    	if (percent > 0) {
    		Log.i("MediaPlayer", "onBufferingUpdate (" + percent + ")");
    	}
    	
		
	}
    
    public void destroyMediaPlayer() {
    	if (mediaPlayer != null){
        	mediaPlayer.reset();
        	mediaPlayer.release();
        	mediaPlayer = null;
        }
    }

    private class SettingsContentObserver extends ContentObserver {
        int previousVolume;
        Context context;

        SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context = c;

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            int currentVolume = getAudioStreamVolume();
//          Log.i("currentVolume", "" + currentVolume);
            volumeSeekBar.setProgress(currentVolume);

            int delta = previousVolume - currentVolume;
            

            if (delta > 0) {
//              Log.i("Decreased", "Decreased");
                previousVolume = currentVolume;
            } else if (delta < 0) {
//              Log.i("Increased", "Increased");
                previousVolume = currentVolume;
            }
        }
        
    }
    
    public int getAudioStreamVolume() {
    	AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager != null ? audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 100;
        
        // 0 to 15
        int max = 15;
        return Math.round((currentVolume * 100) / max);
    }
    
    // R&D
    
    // http://blog.infidian.com/2008/04/04/tutorial-custom-media-streaming-for-androids-mediaplayer/
    // http://www.infidian.com/2009/12/27/android-streaming-mediaplayer-tutorial-updated-to-v1-5-cupcake/
    // http://www.infidian.com/2012/03/04/live-and-progressive-streaming-with-android/
    // https://www.google.fr/?gws_rd=cr&ei=VBXhUu_7BeKc0AXL_YHwDQ#q=android%20stream%20audio%20mediaplayer&safe=off
    // http://stackoverflow.com/questions/1965784/streaming-audio-from-a-url-in-android-using-mediaplayer
    // http://stackoverflow.com/questions/8681550/android-2-2-mediaplayer-is-working-fine-with-one-shoutcast-url-but-not-with-the
    // http://www.speakingcode.com/2012/02/22/creating-a-streaming-audio-app-for-android-with-android-media-mediaplayer-android-media-audiomanager-and-android-app-service/
    // http://androidstreamingtut.blogspot.fr/2012/08/custom-progressive-audio-streaming-with.html

    // http://stackoverflow.com/questions/3595491/is-android-2-2-http-progressive-streaming-http-live-streaming
    
    // AACPLAYER
    // https://code.google.com/p/aacplayer-android/
    // https://code.google.com/p/aacdecoder-android/source/browse/trunk/player/src/com/spoledge/aacplay/AACPlayerActivity.java
    
    // http://stackoverflow.com/questions/6730702/android-playing-stream-m3u-using-mediaplayer
    // http://stackoverflow.com/questions/3595491/is-android-2-2-http-progressive-streaming-http-live-streaming
    // http://stackoverflow.com/questions/6688758/streaming-m3u-audio
    // http://stackoverflow.com/questions/6283568/online-radio-streaming-app-for-android
    
    // http://justdevelopment.blogspot.fr/2009/10/video-streaming-with-android-phone.html
    
    // http://stackoverflow.com/questions/19810968/how-to-do-time-shifting-of-live-audio-stream-on-android
    
    // SoundPool
    // http://stackoverflow.com/questions/10849961/speed-control-of-mediaplayer-in-android
    // http://developer.android.com/reference/android/media/SoundPool.html#setRate%28int,%20float%29
    // http://stackoverflow.com/questions/2751309/change-the-playback-rate-of-a-track-in-real-time-on-android
    
    // http://www.jwplayer.com/blog/the-pain-of-live-streaming-on-android/
    // http://www.streamingmedia.com/Articles/Editorial/Featured-Articles/HTTP-Streaming-What-You-Need-to-Know-65749.aspx
    
}
