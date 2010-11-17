package org.opengeotracker.android.test;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

import org.opengeotracker.android.Constants;
import org.opengeotracker.android.gips.GiPSExtraData;

import android.location.Location;

/**
 * Here we'll put some constants and methods that are used often for testing
 * 
 */
public class TestConstants {
    // 10.0.2.2 = the ip address of the host machine running the emulator
    // Recommended: run a webserver on localhost, and put the server code in a
    // directory called: "OpenGeoTracker"
    public static final String SERVER_URL = "http://10.0.2.2/OpenGeoTracker/";
    public static final String KEY = "key";
    public static final String TAG = "tag";

    public static final String TESTLOCATION = "1282914000000"
	    + Constants.CSV_SEPARATOR + "11.11" + Constants.CSV_SEPARATOR
	    + "22.123456789012" + Constants.CSV_SEPARATOR + "654.0"
	    + Constants.CSV_SEPARATOR + "128.64" + Constants.CSV_SEPARATOR
	    + "222.22" + Constants.CSV_SEPARATOR + "55.11"
	    + Constants.CSV_SEPARATOR + "gps";
    public static final String TESTURL = getTestUrl();

    public static String getTestUrl()
    {
    	DecimalFormat df = new DecimalFormat("#.############"); // 12 digits
       	DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
    	dfs.setDecimalSeparator(Constants.DECIMAL_SEPARATOR);
    	df.setDecimalFormatSymbols(dfs);
    	
    	StringBuffer sb = new StringBuffer();
    	Location l = TestConstants.getTestLocation();
    	sb.append('&').append("time1="+URLEncoder.encode("2010-08-27T15:00:00.00+01:00"));   	
    	sb.append('&').append("latitude1="+df.format(l.getLatitude()));
    	sb.append('&').append("longitude1="+df.format(l.getLongitude()));
    	sb.append('&').append("provider1="+l.getProvider());
    	sb.append('&').append("altitude1="+df.format(l.getAltitude()));
    	sb.append('&').append("speed1="+df.format(l.getSpeed()));
    	sb.append('&').append("bearing1="+df.format(l.getBearing()));
    	sb.append('&').append("accuracy1="+df.format(l.getAccuracy()));
    	
    	return sb.toString();
    }
    
    
    public static Calendar getTestCalendar() {
	Calendar c = Calendar.getInstance();
	c.set(Calendar.YEAR, 2010);
	c.set(Calendar.MONTH, 7); // starts counting from 0
	c.set(Calendar.DAY_OF_MONTH, 27);
	c.set(Calendar.HOUR_OF_DAY, 15);
	c.set(Calendar.MINUTE, 00);
	c.set(Calendar.SECOND, 00);
	c.set(Calendar.MILLISECOND, 00);
	return c;
    }

    public static Location getTestLocation() {
	Location location = new Location("getTestLocation");
	location.setTime(TestConstants.getTestCalendar().getTimeInMillis());
	location.setLatitude(11.11);
	location.setLongitude(22.123456789012);
	location.setProvider("gps");
	location.setAccuracy(55.11f);
	location.setAltitude(654);
	location.setBearing(128.64f);
	location.setSpeed(222.22f);
	return location;
    }
    
    public static GiPSExtraData getTestGipsdata() {
    	GiPSExtraData oneGiPSData = new GiPSExtraData();
    	oneGiPSData.key = "provakey";
    	oneGiPSData.tag = "provatag";
    	oneGiPSData.buttoncode = "provabuttoncode";
    	
    	return oneGiPSData;
   }
}
