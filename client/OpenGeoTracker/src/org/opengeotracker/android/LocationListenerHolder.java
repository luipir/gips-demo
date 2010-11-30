package org.opengeotracker.android;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

/**
 * 
 * @author Johan Ekblad &lt;jka@opengeotracker.org&gt;
 * @license GNU General Public License (GPL) version 2 (see: http://www.gnu.org
 *          /licenses/gpl-2.0.txt)
 */

public class LocationListenerHolder {
    private static final String LOGTAG = "LocationListenerHolder";
    private static LocationManager lm;
    private static GeoLocationListener gll;
    
    // start logging points peridically
    public static void startLogging(
    		Activity a, 
    		Context ctx, 
    		String keyId,
    		int interval, 
    		String url, 
    		String tag, 
    		String unit) {
		
    	lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		gll = new GeoLocationListener(a, lm, ctx, keyId, interval, url, tag, unit, false);
		
		Log.d(LOGTAG, "Requesting location updates for startLogging, every "
			+ interval * 1000 + " millis.");
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,	interval * 1000, 0, gll);
		//lm.addGpsStatusListener(gll.gpsListener);
		gll.start();
    }
    
    // flushes points archived in DB
    public static void startFlushing(
    		Activity a, 
    		Context ctx, 
    		String keyId,
    		int interval, 
    		String url, 
    		String tag, 
    		String unit) {
		
    	lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		gll = new GeoLocationListener(a, lm, ctx, keyId, interval, url);
		
		Log.d(LOGTAG, "Requesting location updates for startFlushing, every "
			+ interval * 1000 + " millis.");
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,	interval * 1000, 0, gll);
		//lm.addGpsStatusListener(gll.gpsListener);
		gll.start();
    }
    
    // record current point in the local db
    public static void pointLogging(
    		Context ctx, 
    		String keyId, 
    		int interval,
    		String url, 
    		String tag, 
    		String buttoncode) {
    	lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		gll = new GeoLocationListener(lm, ctx, keyId, interval, url, tag, true, buttoncode);
		Log.d(LOGTAG, "Requesting location updates for pointLogging, every "
			+ interval * 1000 + " millis.");
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval * 1000, 0, gll);
		//lm.addGpsStatusListener(gll.gpsListener);
    }

    public static void stopLogging() {
		if (gll != null && lm != null) {
		    lm.removeUpdates(gll);
		    gll.stopRunning();
		    lm = null;
		}
    }
}
