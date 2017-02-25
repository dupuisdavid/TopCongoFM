package com.dnd.radioTopCongo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dnd.radioTopCongo.toolbox.DisplayUtils;
import com.dnd.radioTopCongo.toolbox.StreamingMediaPlayer;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;

public class MainPlayerV2Activity extends Activity {
	
	private MainPlayerV2Activity activity = this;
	
	private FrameLayout wrapperView;
	private SeekBar volumeSeekBar;
	
	private ImageButton playPauseButton;
	private TextView textStreamed;
	private boolean isPlaying;
	private StreamingMediaPlayer audioStreamer;
	private ProgressBar progressBar;
	
    @SuppressWarnings("unused")
	private TextView playerStatusTextView;
    
    private String STREAM_URI;
    private int currentVolume;

    // REWIND, FAST FORWARD
    // https://groups.google.com/forum/#!topic/android-porting/PuTWU6fAjYY

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_player_activity);
		
		
		STREAM_URI = getResources().getString(R.string.radioTopCongoStreamURL);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
        currentVolume = getAudioStreamVolume();
//      Log.i("currentVolume", "" + currentVolume);
		
		Boolean displayScreenCharacteristics = false;
        // DISPLAY INFORMATIONS
		if (displayScreenCharacteristics) {
			getDisplayInformations();
		}
        
        
		// GET UI ELEMENTS
        wrapperView = (FrameLayout) findViewById(R.id.wrapperView);
        playerStatusTextView = (TextView) findViewById(R.id.playerStatusTextView);
        
        initContactButton();
        initInformationButton();
        // INIT CONTROLS
        initControls();

        // VOLUME OBSERVER
        SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
        
        startStreamingAudio();

	}
	
	@Override
    protected void onStart() {
        super.onStart();

		TopCongoApplication application = (TopCongoApplication) getApplication();
		Tracker tracker = application.getDefaultTracker();

		if (tracker != null) {
			tracker.setScreenName("PlayerView");
			tracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
    }
	
	private void getDisplayInformations() {
		// Device display properties
        Display display = getWindowManager().getDefaultDisplay();
        // Screen size
        int[] sizes = DisplayUtils.getSizes((Context) this);
        // Screen density
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        // Logs
        Log.i("DEVICE SCREEN RESOLUTION", sizes[0] + "x" + sizes[1]);
        
        // 0.75 - LDPI, 1.0 - MDPI, 1.5 - HDPI, 2.0 - XHDPI, 3.0 - XXHDPI, 4.0 - XXXHDPI
        Log.i("DEVICE DENSITY", "density :" +  metrics.density);			
        Log.i("DEVICE DENSITY", "D density :" +  metrics.densityDpi);		// density interms of dpi
        Log.i("DEVICE DENSITY", "width pix :" +  metrics.widthPixels);		// horizontal pixel resolution
        Log.i("DEVICE DENSITY", "xdpi :" +  metrics.xdpi);					// actual horizontal dpi
        Log.i("DEVICE DENSITY", "ydpi :" +  metrics.ydpi);					// actual vertical dpi
	}
	
	private void initContactButton() {
	
		ImageButton contactButton = (ImageButton) wrapperView.findViewById(R.id.contactButton);
		contactButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(activity, ContactActivity.class);
            	startActivity(intent);
				
			}
		});
		
	}
	
	private void initInformationButton() {
		
		ImageButton informationButton = (ImageButton) wrapperView.findViewById(R.id.informationButton);
		informationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(activity, InformationActivity.class);
            	startActivity(intent);
				
			}
		});
		
	}
	
	private void initControls() {
		
    	textStreamed = (TextView) findViewById(R.id.textNbKbStreamed);
    	
    	playPauseButton = (ImageButton) wrapperView.findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("isPlaying", "" + isPlaying);
				
				if (audioStreamer != null && audioStreamer.getMediaPlayer() != null) {
					if (audioStreamer.getMediaPlayer().isPlaying()) {
						audioStreamer.getMediaPlayer().pause();
						switchToPlayImageButtonDrawable();
					} else {
						audioStreamer.getMediaPlayer().start();
						audioStreamer.startPlayProgressUpdater();
						switchToPauseImageButtonDrawable();
					}
					
					isPlaying = !isPlaying;
				} else {
					
					Log.i("Error", "audioStreamer or audioStreamer.getMediaPlayer() is null");
					
				}
				
				
			}
		});
        
        // VOLUME SEEK BAR
        volumeSeekBar = (SeekBar) wrapperView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {				
				float volume = (float) (progress/100.0);
//				Log.i("Volume", "" + volume);
				
				MediaPlayer mediaPlayer = audioStreamer.getMediaPlayer();
				
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
    }

	private void startStreamingAudio() {
    	
		try {
    		Log.i("startStreamingAudio", "startStreamingAudio");
    		
    		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    		
    		if (audioStreamer != null) {
    			audioStreamer.interrupt();
    		}
    		
    		audioStreamer = new StreamingMediaPlayer(this, textStreamed, playPauseButton, progressBar);
    		audioStreamer.startStreaming(STREAM_URI, 5208, 216);

    		switchToPauseImageButtonDrawable();
    		
    	} catch (IOException e) {
	    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
    	}
    	    	
    }
	
	private void switchToPlayImageButtonDrawable() {
		playPauseButton.setImageResource(R.drawable.play);
	}
	
	private void switchToPauseImageButtonDrawable() {
		playPauseButton.setImageResource(R.drawable.pause);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

    public class SettingsContentObserver extends ContentObserver {
        int previousVolume;
        Context context;

        public SettingsContentObserver(Context c, Handler handler) {
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
                Log.i("Decreased", "Decreased");
                previousVolume = currentVolume;
            } else if (delta < 0) {
                Log.i("Increased", "Increased");
                previousVolume = currentVolume;
            }
        }
    }
    
    public int getAudioStreamVolume() {
    	AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        // 0 to 15
        int max = 15;
        int volumePercentage = Math.round((currentVolume * 100) / max);
        
        
        return volumePercentage;
    }
}
