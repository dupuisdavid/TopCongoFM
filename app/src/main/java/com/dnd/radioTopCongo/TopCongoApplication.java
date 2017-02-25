package com.dnd.radioTopCongo;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onesignal.OneSignal;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

public class TopCongoApplication extends Application {

    private static final String TAG = TopCongoApplication.class.getSimpleName();
	private MainPlayerActivity mainPlayerActivity;

	private Tracker mTracker;
    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
	
	public MainPlayerActivity getMainPlayerActivity() {
		return mainPlayerActivity;
	}

	public void setMainPlayerActivity(MainPlayerActivity mainPlayerActivity) {
		this.mainPlayerActivity = mainPlayerActivity;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG) {
			Log.i(TAG, "onCreate");
		}

        setupOneSignal();
        setupFabric();
		setupUniversalImageLoader();
	}

    private void setupOneSignal() {
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationOpenedHandler(this))
                .init();
    }

    private void setupFabric() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(Config.TWITTER_KEY, Config.TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
    }
	

	public void setupUniversalImageLoader() {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024) // 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
//				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

}
