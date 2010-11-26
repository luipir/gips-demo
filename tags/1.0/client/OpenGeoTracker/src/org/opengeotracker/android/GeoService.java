package org.opengeotracker.android;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Easy to use, easy to predict Service; when the service is started, GPS
 * tracking starts. When it's ended, GPS tracking ends.
 * 
 * @author tom
 * 
 */
public class GeoService extends Service {
    private static final String TAG = "GeoService";
    private Handler handler = new Handler();
    private int update_interval;
    private Boolean isLogging = false;
    private Activity a;

    public void startLogging(Activity a) {
		if (isLogging() != null && isLogging()) {
		    Log.d(TAG, "We where already logging!");
		}
		this.a = a;
		// load preferences
		SharedPreferences mPrefs = getSharedPreferences(
			Constants.PREFERENCESFILE, Context.MODE_PRIVATE);
		String url = mPrefs.getString(Constants.URL, Constants.DEFAULT_URL);
		String key = mPrefs.getString(Constants.KEY_ID,
			Constants.DEFAULT_KEY_ID);
		String tag = mPrefs.getString(Constants.TAG, Constants.DEFAULT_TAG);
		String unit = mPrefs.getString(Constants.UNIT, Constants.DEFAULT_UNIT);
		update_interval = mPrefs.getInt(Constants.UPDATE_INTERVAL,
			Constants.DEFAULT_UPDATE_INTERVAL);
	
		// start logging locations
		Log.d(TAG, "Calling LocationListenerHolder.startLogging(context," + key
			+ "," + update_interval + "," + url + "," + tag + ")");
		LocationListenerHolder.startLogging(a, getApplicationContext(), key,
			update_interval, url, tag, unit);
	
		// remember that we've started logging
		isLogging = true;
    }

    public void stopLogging() {
		if (isLogging() != null && !isLogging()) {
		    Log.d(TAG, "We have already turned off logging!");
		}
		// stop logging locations
		LocationListenerHolder.stopLogging();
	
		// remember that we stopped logging
		isLogging = false;
    }

    @Override
    public void onCreate() {
		super.onCreate();
		Log.d(TAG, "GeoService created");
    }

    @Override
    public void onLowMemory() {
		Log.w(TAG, "Warning: the system reports that it's low on memory. "
			+ "Please don't kill me !");
    }

    @Override
    public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "GeoService's onStart called");
	
		// Make sure logging is started along with the service, because
		// BootReceiver itself does not call .startLogging()
		if (!isLogging) {
		    startLogging(a);
		}
	
		SharedPreferences mPrefs = getSharedPreferences(
			Constants.PREFERENCESFILE, Context.MODE_PRIVATE);
		update_interval = mPrefs.getInt(Constants.UPDATE_INTERVAL,
			Constants.DEFAULT_UPDATE_INTERVAL);
	
		// check bacd s
		handler.postDelayed(doLogTasks, update_interval * 1000);
    }

    // This method is only called when there are not more connections to the
    // service, AND the stopService() method has been called.
    @Override
    public void onDestroy() {
		super.onDestroy();
	
		stopLogging();
	
		Log.d(TAG, "GeoService destroyed");
	
		// stop the heartbeat
		handler.removeCallbacks(doLogTasks);
    }

    private Runnable doLogTasks = new Runnable() {
		public void run() {
		    // the update_interval variable is accessible because "Anonymous
		    // inner classes are scoped inside the private
		    // scoping of the outer class. They can access the internal
		    // (private) properties and methods of the outer class."
		    handler.postDelayed(this, update_interval * 1000);
		}
    };

    public Boolean isLogging() {
    	return isLogging;
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
    	return mBinder;
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
		GeoService getService() {
		    return GeoService.this;
		}
    }
}
