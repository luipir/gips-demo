package org.opengeotracker.android;

import java.net.URLEncoder;

import org.opengeotracker.android.preference.Preferences;
import org.opengeotracker.android.gips.Observation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Improvements:
 * 
 * 1) Select tagging-categories from a drop-down list (new tagging-categories
 * might be added) 2) Update a counter with saved points so far
 * 
 * 
 * @author Johan Ekblad &lt;jka@opengeotracker.org&gt;
 * @license GNU General Public License (GPL) version 2 (see:
 *          http://www.gnu.org/licenses/gpl-2.0.txt)
 */

public class OpenGeoTracker extends Activity {
    private static final String LOGTAG = "OpenGeoTracker";
    private static final int PREFERENCES_ID = Menu.FIRST;

    // user's preferences:
    int nint;
    String url;
    String key;
    String tag;
    String unit;
    public Activity a;
    private GeoService boundGeoService; // Talk to GeoService

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, PREFERENCES_ID, 2, R.string.preferences).setIcon(
			android.R.drawable.ic_menu_preferences);
		return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case PREFERENCES_ID:
		    Intent preferencesActivity = new Intent(OpenGeoTracker.this,
			    Preferences.class);
		    OpenGeoTracker.this.startActivity(preferencesActivity);
		    return true;
		}
		return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Load the preferences and put them in global variables
     */
    private void loadPrefs() {
		SharedPreferences mPrefs = getSharedPreferences(Constants.PREFERENCESFILE, MODE_PRIVATE);
		url = mPrefs.getString(Constants.URL, Constants.DEFAULT_URL);
		key = mPrefs.getString(Constants.KEY_ID, Constants.DEFAULT_KEY_ID);
		nint = mPrefs.getInt(Constants.UPDATE_INTERVAL,	Constants.DEFAULT_UPDATE_INTERVAL);
		tag = mPrefs.getString(Constants.TAG, Constants.DEFAULT_TAG);
		unit = mPrefs.getString(Constants.UNIT, Constants.DEFAULT_UNIT);
    }

    /**
     * Returnes the base dir of the url (only the text logPos.php is removed,
     * for backward compability):
     * 
     * Examples: url=http://opengeotracker.org/logPos.php =>
     * http://opengeotracker.org/ url=http://opengeotracker.org =>
     * http://opengeotracker.org/ url=http://myurl.org/hello/logPos.php =>
     * http://myurl.org/hello/ url=http://myurl.org/hello/ =>
     * http://myurl.org/hello/ url=http://myurl.org/hello =>
     * http://myurl.org/hello/
     * 
     */
    private String getUrl() {
		String res = new String(url);
		int logPos = res.indexOf("logPos.php");
		if (logPos >= 0) {
		    res = res.substring(0, logPos); // just keep everything up until
		    // logPos.php
		}
		if (res.length() > 0 && res.charAt(res.length() - 1) != '/') {
		    res = res + "/";
		}
		return res;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		loadPrefs();
	
		EditText editText = (EditText) findViewById(R.id.tagName);
		editText.setText(tag);
	
		// bind to the service, so we have the object already, even if the
		// service hasn't been started yet
		doBindService();
		
		// update text during edit
		((EditText) findViewById(R.id.tagName)).setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				tag = ((EditText) findViewById(R.id.tagName)).getText().toString();

				// save tag to preferences
				SharedPreferences.Editor editor = getSharedPreferences(
					Constants.PREFERENCESFILE, Context.MODE_PRIVATE)
					.edit();
				tag = ((EditText) findViewById(R.id.tagName)).getText().toString();
				editor.putString(Constants.TAG, tag);
				editor.commit();
				
				return false;
			}
		});
	
		// All this is for the "Start sending" / "Stop sending" button logic
		((Button) findViewById(R.id.points)).setOnClickListener(new View.OnClickListener() {
			    public void onClick(View view) {
					// Enable/disable logging
					if (boundGeoService != null
						&& boundGeoService.isLogging() != null
						&& boundGeoService.isLogging()) {
					    Log.d(LOGTAG, "stopLogging/stopService...");
					    Intent svc = new Intent(getApplicationContext(),
						    GeoService.class);
					    boundGeoService.stopLogging();
					    stopService(svc);
					    Log.d(LOGTAG, "done stop");
					} else {
					    Log.d(LOGTAG, "startLogging/startService...");
		
					    Intent svc = new Intent(getApplicationContext(),
						    GeoService.class);
		
					    if (boundGeoService != null) {
					    	boundGeoService.startLogging(a);
					    }
		
					    startService(svc);
					    Log.d(LOGTAG, "done start");
					}
		
					// adjust the button text accordingly
					updateStartStopSendingButton();
			    }
		});
	
		// All this is for the "Send point" button:
		Button sendPoint = (Button) findViewById(R.id.sendPoint);
		sendPoint.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View view) {
		    	// open activity
				Log.d(LOGTAG, "Opening SendPoint Activity");
				Intent observationIntent = new Intent(getApplicationContext(), Observation.class);
	
				// prepare data to send to activity
				Bundle extradata = new Bundle();
				extradata.putInt("nint", nint);
				extradata.putString("url", url);
				extradata.putString("key", key);
				extradata.putString("tag", tag);
				extradata.putString("unit", unit);
				observationIntent.putExtra("extradata", extradata);
				
				// start activity
				startActivity(observationIntent);
		    }
		});
	
		// All this is for the "View Map" button
		((Button) findViewById(R.id.urlMap)).setOnClickListener(new View.OnClickListener() {
			    public void onClick(View view) {
					EditText editText = (EditText) findViewById(R.id.tagName);
					tag = editText.getText().toString();
					Log.d(LOGTAG, "Opening webpage: "
						+ Uri.parse(getUrl() + "showPos.php?key="
							+ URLEncoder.encode(key) + "&tag="
							+ URLEncoder.encode(tag)));
					Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(getUrl() + "showPos.php?key="
							+ URLEncoder.encode(key) + "&tag="
							+ URLEncoder.encode(tag)));
					startActivity(myIntent);
			    }
		});
	
		// All this is for the "View key page" button
		((Button) findViewById(R.id.urlKeyPage)).setOnClickListener(new View.OnClickListener() {
			    public void onClick(View view) {
					Log.d(LOGTAG, "Opening webpage: "
						+ Uri.parse(getUrl() + "keyInfo.php?key="
							+ URLEncoder.encode(key)));
					Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(getUrl() + "keyInfo.php?key="
							+ URLEncoder.encode(key)));
					startActivity(myIntent);
			    }
		});
    }

    @Override
    protected void onStart() {
		super.onStart();
		updateStartStopSendingButton();
    }

    @Override
    protected void onResume() {
		super.onResume();
		updateStartStopSendingButton();
    }

    /**
     * Persist state when the Activity is paused, since it then risks being
     * killed by the system.
     */
    @Override
    protected void onPause() {
		super.onPause();
	
		SharedPreferences mPrefs = getSharedPreferences(Constants.PREFERENCESFILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = mPrefs.edit();
	
		url = mPrefs.getString(Constants.URL, Constants.DEFAULT_URL);
		key = mPrefs.getString(Constants.KEY_ID, Constants.DEFAULT_KEY_ID);
		nint = mPrefs.getInt(Constants.UPDATE_INTERVAL, Constants.DEFAULT_UPDATE_INTERVAL);
		tag = mPrefs.getString(Constants.TAG, Constants.DEFAULT_TAG);
		unit = mPrefs.getString(Constants.UNIT, Constants.DEFAULT_UNIT);
	
		editor.putString(Constants.URL, getUrl()); // backward compatibility if
		// URL ends with logPos.php
		editor.putString(Constants.KEY_ID, key);
		editor.putInt(Constants.UPDATE_INTERVAL, nint);
		editor.putString(Constants.TAG, tag);
		editor.putString(Constants.UNIT, unit);
		editor.commit();
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
		    // This is called when the connection with the service has been
		    // established, giving us the service object we can use to
		    // interact with the service. Because we have bound to a explicit
		    // service that we know is running in our own process, we can
		    // cast its IBinder to a concrete class and directly access it.
		    boundGeoService = ((GeoService.LocalBinder) service).getService();
	
		    // Tell the user about this for our demo.
		    Log.v(LOGTAG, "local_service_connected");
	
		    updateStartStopSendingButton();
		}

		public void onServiceDisconnected(ComponentName className) {
		    // This is called when the connection with the service has been
		    // unexpectedly disconnected -- that is, its process crashed.
		    // Because it is running in our same process, we should never
		    // see this happen.
		    boundGeoService = null;
		    Log.v(LOGTAG, "local_service_disconnected");
		}
    };

    protected void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		Intent geoIntent = new Intent(getApplicationContext(), GeoService.class);
		bindService(geoIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
		if (boundGeoService != null) {
		    // Detach our existing connection.
		    unbindService(mConnection);
		}
    }

    @Override
    protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
    }

    private void updateStartStopSendingButton() {
		// update the "Start sending" button, if we're already logging
		if (boundGeoService == null) {
		    ((Button) findViewById(R.id.points)).setText("Unknown");
		} else if (boundGeoService.isLogging() != null
			&& boundGeoService.isLogging()) {
		    ((Button) findViewById(R.id.points)).setText("Stop sending");
		    ((TextView) findViewById(R.id.distanceLabel)).setText("Distance:");
		    ((TextView) findViewById(R.id.distanceValue)).setText("");
		} else {
		    ((Button) findViewById(R.id.points)).setText("Start sending");
		    ((TextView) findViewById(R.id.distanceLabel)).setText("");
		    ((TextView) findViewById(R.id.distanceValue)).setText("");
		}
    }
}