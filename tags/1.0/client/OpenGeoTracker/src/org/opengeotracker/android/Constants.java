package org.opengeotracker.android;

import java.util.Random;

/**
 * This class holds all constants for this project.
 * 
 */
public class Constants {
	// GiPS Costants
    public static final String DBNAME = "OpenGeoTracker.db";
    public static final String VIBRATE_MODE = "vibrate_enable";
    public static final String VIBRATE_INTERVAL = "vibrate_interval";
    public static final int VIBRATE_DEFAULT_INTERVAL = 100;
    public static final String CHUNK_DIMENSION = "chunk";
	
    public static final String TABLE_NAME = "gipsobservations";
    public static final String DBCOLUMNS_sent = "sent";
    public static final String DBCOLUMNS_acqdate = "acqdate";
    public static final String DBCOLUMNS_lat = "lat";
    public static final String DBCOLUMNS_lon = "lon";
    public static final String DBCOLUMNS_altitude = "altitude";
    public static final String DBCOLUMNS_bearing = "bearing";
    public static final String DBCOLUMNS_speed = "speed";
    public static final String DBCOLUMNS_accuracy = "accuracy";
    public static final String DBCOLUMNS_provider = "provider";
    public static final String DBCOLUMNS_key = "key";
    public static final String DBCOLUMNS_tag = "tag";
    public static final String DBCOLUMNS_buttoncode = "buttoncode";

    // original constants
    public static final String PREFERENCESFILE = "OGTfile";
    public static final String SERVICENAME = "org.opengeotracker.android.GeoService";
    public static final String LOCALFILENAME = "OpenGeoTracker.csv";
    public static final String CSV_SEPARATOR = ",";
    public static final int MAX_LOCATIONS_PER_HTTP_POST = 25;
    public static final char DECIMAL_SEPARATOR = new Character('.');
    // preferences:
    public static final String URL = "url";
    public static final String KEY_ID = "key_id";
    public static final String UPDATE_INTERVAL = "nint";
    public static final String TAG = "tag";
    public static final String STARTUPATBOOT = "startupatboot";
    public static final String UNIT = "unit";
    public static final String OFFLINE_MODE = "offlinemode_enable";
    public static final String OFFLINE_MODE_NEVER_DELETE = "neverdelete_enable";
    // default values:
    public static final String DEFAULT_URL = "http://gipsin.it/";
    public static final String DEFAULT_SERVICE = "logManyPos.php";
    public static final String DEFAULT_KEY_ID = generateRandomKeyId();
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final String DEFAULT_TAG = "test";
    public static final String DEFAULT_UNIT = "kilometers";

    private static String generateRandomKeyId() {
	String universe = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrtsuvwxyz0123456789";
	int length = 8;
	Random rnd = new Random();
	StringBuffer sb = new StringBuffer();

	for (int i = 0; i < length; i++) {
	    sb.append(universe.charAt(rnd.nextInt(universe.length())));
	}

	return sb.toString();

    }
}