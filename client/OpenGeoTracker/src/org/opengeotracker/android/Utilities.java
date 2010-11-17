package org.opengeotracker.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.location.Location;
import android.util.Log;

public class Utilities {
	
	final static String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSZ";
	final static String LOGTAG = "Utilities";
	
    /**
     * Generate a Calendar from ISO 8601 date
     * 
     * @param date
     *            a ISO 8601 Date string
     * @return a Calendar object
     */
	public static Calendar getCalendarFromISO(String datestring) {
		
		Log.d(LOGTAG, "Date string to convert: "+datestring);
		
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()) ;
		SimpleDateFormat dateformat = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
		try {
			Date date = dateformat.parse(datestring);
			date.setHours(date.getHours()-1); // patch because I don't know why there is an hour more! ??????????????
			calendar.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Log.d(LOGTAG, "Calendar converted ="+calendar.toString());
		
		return calendar;
	}
	
    /**
     * Generate a ISO 8601 date
     * 
     * @param date
     *            a Date instance
     * @return a string representing the date in the ISO 8601 format
     */
    public static String getIsoDate(Calendar calendar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(calendar.get(Calendar.YEAR));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
		buffer.append("-");
		buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
		buffer.append("T");
		buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
		buffer.append(":");
		buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
		buffer.append(".");
		buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
		// buffer.append("Z");
		int offset = calendar.get(Calendar.ZONE_OFFSET) / (1000 * 60 * 60);
		if (offset > 0) {
		    buffer.append("+");
		} else {
		    buffer.append("-");
		}
		buffer.append(twoDigit(offset));
		buffer.append(":00");
		return buffer.toString();
	    }
	
	    public static String twoDigit(int i) {
		if (i >= 0 && i < 10) {
		    return "0" + String.valueOf(i);
		}
		return String.valueOf(i);
    }

    /**
     * Calculate distance between two lat-lng pairs, The earth is approximated
     * as a sphere
     * 
     * @param thislat
     * @param thislng
     * @param lastlat
     * @param lastlng
     * @return
     */
    public static double calcDistance(double thislat, double thislng,
	    double lastlat, double lastlng) {
		double rad = Math.PI / 180.0d;
		double lat1 = lastlat * rad;
		double lng1 = lastlng * rad;
		double lat2 = thislat * rad;
		double lng2 = thislng * rad;
	
		double x = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
			* Math.cos(lat2) * Math.cos(lng1 - lng2);
		if (x < 1.0) {
		    return 6371.2d * Math.acos(x);
		} else
		    return 0.0d;
    }

    /**
     * The order in which these fields are stored is: - time - latitude -
     * longitude - altitude - bearing - speed - accuracy - provider
     * 
     * @param the
     *            location object
     * @return the location CSV string
     */
    public static String locationToString(Location loc) {
		return loc.getTime() + Constants.CSV_SEPARATOR + loc.getLatitude()
			+ Constants.CSV_SEPARATOR + loc.getLongitude()
			+ Constants.CSV_SEPARATOR + loc.getAltitude()
			+ Constants.CSV_SEPARATOR + loc.getBearing()
			+ Constants.CSV_SEPARATOR + loc.getSpeed()
			+ Constants.CSV_SEPARATOR + loc.getAccuracy()
			+ Constants.CSV_SEPARATOR + loc.getProvider();
    }

    /**
     * The order in which these fields are stored is: - time - latitude -
     * longitude - altitude - bearing - speed - accuracy - provider
     * 
     * @param the
     *            location CSV string
     * @return the location object
     */
    public static Location stringToLocation(String nextLine) {
		// split the line
		String[] fields = nextLine.split(Constants.CSV_SEPARATOR);
	
		Location l = new Location("offline location storage");
		int fieldCounter = 0;
		l.setTime(new Long(fields[fieldCounter++]));
		l.setLatitude(new Double(fields[fieldCounter++]));
		l.setLongitude(new Double(fields[fieldCounter++]));
		l.setAltitude(new Double(fields[fieldCounter++]));
		l.setBearing(new Float(fields[fieldCounter++]));
		l.setSpeed(new Float(fields[fieldCounter++]));
		l.setAccuracy(new Float(fields[fieldCounter++]));
		l.setProvider(fields[fieldCounter++]);
		return l;
    }

    public static String fileToString(File f) throws IOException {
		String fileString = "";
		String line;
		BufferedReader file = new BufferedReader(new FileReader(f));
		while ((line = file.readLine()) != null) {
		    fileString += line;
		}
		return fileString;
    }

    /**
     * All properties must be equal, except for provider and extras.
     * 
     * @param l1
     * @param l2
     * @return whether or not these locations are equal
     */
    public static boolean locationEquals(Location l1, Location l2) {
		return l1.getTime() == l2.getTime()
			&& l1.getLatitude() == l2.getLatitude()
			&& l1.getLongitude() == l2.getLongitude()
			&& l1.getAltitude() == l2.getAltitude()
			&& l1.getAccuracy() == l2.getAccuracy()
			&& l1.getBearing() == l2.getBearing()
			&& l1.getSpeed() == l2.getSpeed();
    }
}
