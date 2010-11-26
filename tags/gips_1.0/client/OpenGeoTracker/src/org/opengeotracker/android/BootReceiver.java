package org.opengeotracker.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private final static String LOGTAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d(LOGTAG,
		"BootReceiver.onReceive(), probably ACTION_BOOT_COMPLETED");

	// start on BOOT_COMPLETED
	if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
	    Log.d(LOGTAG, "BootReceiver received ACTION_BOOT_COMPLETED");

	    // check in the settings if we need to auto start
	    if (context.getSharedPreferences(Constants.PREFERENCESFILE,
		    Context.MODE_PRIVATE).getBoolean(Constants.STARTUPATBOOT,
		    false)) {
		Log.d(LOGTAG, "Starting " + Constants.SERVICENAME);
		Intent newintent = new Intent(context, GeoService.class);
		context.startService(newintent);
	    } else {
		Log.d(LOGTAG, "Not starting " + Constants.SERVICENAME
			+ "; Adjust the settings if you wanted this !");
	    }
	}
    }

}
