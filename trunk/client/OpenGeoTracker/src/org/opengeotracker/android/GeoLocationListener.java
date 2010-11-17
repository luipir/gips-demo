package org.opengeotracker.android;

import org.opengeotracker.android.gips.GiPSExtraData;
import org.opengeotracker.android.gips.GiPSDB;

import org.opengeotracker.android.R;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Johan Ekblad &lt;jka@opengeotracker.org&gt;
 * @license GNU General Public License (GPL) version 2 (see:
 *          http://www.gnu.org/licenses/gpl-2.0.txt)
 */
public class GeoLocationListener extends Thread implements LocationListener {
    private static final String LOGTAG = "org.opengeotracker.android.GeoLocationListener";

	GiPSDB gipsdb_helper;
	SQLiteDatabase gipsdb;
 
	Activity activity;
    Context ctx;
    ConnectivityManager cm = null;
    Vibrator vibrator = null;
    String key;
    int interval;
    boolean isMetric; // Use kilometer=>true, miles=>false
    String url;
    String tag;
    String unit;
    String buttoncode;
    boolean onePoint;
    LocationManager lm;
    Date lastTime = null;
    long count = 0;
    boolean isRunning;
    double lastlat;
    double lastlng;
    double lastdist;
    private DecimalFormat df; // used for converting doubles to strings without
    private ConnectionChangeReceiver connReceiver = null;
    boolean isconnected;
    boolean isSynched;

    // commas
    private void initLocationListener(
    		Activity a, 
    		LocationManager lm,
    		Context ctx, 
    		String key, 
    		int interval, 
    		String url, 
    		String tag,
    		String unit, 
    		boolean onePoint,
    		String buttoncode) {
		this.activity = a;
		this.lm = lm;
		this.ctx = ctx;
		this.key = key;
		this.interval = interval;
		this.url = url;
		this.tag = tag;
		this.onePoint = onePoint;
		this.buttoncode = buttoncode;
		this.count = 0;
		this.isRunning = true;
		this.lastlat = 0.0d;
		this.lastlng = 0.0d;
		this.lastdist = 0.0d;
		this.unit = unit;
		if (unit == null || unit.equals("kilometers")) {
		    this.isMetric = true;
		} else {
		    this.isMetric = false;
		}
		vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// register BroadcastReceiver to monitor internet connection
		connReceiver = new ConnectionChangeReceiver();
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        ctx.registerReceiver(connReceiver, intentFilter);
		
        // create DB to store points in case of offline
		gipsdb_helper = new GiPSDB(ctx, "", null, -1);
		gipsdb = gipsdb_helper.getWritableDatabase();
		
		// use a formatter to make sure we get the the Double uses a dot
		// as a decimal separator
		df = new DecimalFormat("#.############"); // 12 digits
	   	DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
		dfs.setDecimalSeparator(Constants.DECIMAL_SEPARATOR);
		df.setDecimalFormatSymbols(dfs);
    }

    public GeoLocationListener(
    		Activity a, 
    		LocationManager lm, 
    		Context ctx,
    		String key, 
    		int interval, 
    		String url, 
    		String tag, 
    		String unit,
    		boolean onePoint) {
    	initLocationListener(a, lm, ctx, key, interval, url, tag, unit,
			onePoint, "-1");
    }

    public GeoLocationListener(
    		LocationManager lm, 
    		Context ctx, 
    		String key,
    		int interval, 
    		String url, 
    		String tag, 
    		boolean onePoint,
    		String buttoncode) {
		initLocationListener((Activity) null, lm, ctx, key, interval, url, tag,
			(String) null, onePoint, buttoncode);
    }
    
    // set connection status when connection state change receiving
    // Broadcast message
	public class ConnectionChangeReceiver extends BroadcastReceiver {
		
		private NetworkInfo activeNetInfo = null;
	    
		@Override 
	    public void onReceive(Context context, Intent intent) { 
			
	    	isconnected = false;
	    	
	    	activeNetInfo = cm.getActiveNetworkInfo(); 
			if (activeNetInfo != null) {
				isconnected = activeNetInfo.isConnectedOrConnecting();
			}
			
			String msg = "Connection is "+isconnected;
			Log.i(LOGTAG, msg);
	    }
	} 
    
    // this reports on the status of the GPS engine, but does not enable 
	// additional controls
	// !!!!!!!!!!!!!!!!! FUNCTION UNUSED !!!!!!!!!!!!!!!!!
    public final GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            GpsStatus gpsStatus = lm.getGpsStatus(null);
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED: 
                    Log.i(LOGTAG, "onGpsStatusChanged(): GPS started");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX: 
                    Log.i(LOGTAG, "onGpsStatusChanged(): time to first fix in ms = " + gpsStatus.getTimeToFirstFix());
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS: 
                    Log.d(LOGTAG, "onGpsStatusChanged(): GpsStatus.GPS_EVENT_SATELLITE_STATUS"); 
                    break;
                case GpsStatus.GPS_EVENT_STOPPED: 
                    Log.i(LOGTAG, "onGpsStatusChanged(): GPS stopped");
                    break;
            }       
        }
    };
    
    /**
     * Save a location to a server, or to the local buffer.
     * 
     * if we're in offline mode or connection is not available, we just add 
     * this location to the buffer for later uploading 
     * - if we're in online mode, we upload this one location as usual, and 
     * then flush the location buffer We do it in that
     * order because flushing can take a long time, so by design it's more
     * robust than sending a single point
     * 
     * @param Location
     *			loc
     * @param GiPSExtraData
     * 			oneGiPSData
     * @return true if saving (eighter local or to a server) was successful
     */
    private boolean saveLocation(Location loc, GiPSExtraData oneGiPSData) {
    	boolean success = false;
	
		if (loc == null) {
		    Log.w(LOGTAG, "Location object loc == null, we have no location !");
		} else {
		    Log.v(LOGTAG, "Received location, saving or sending it...");
		    
		    // check if we're allowed to upload the location
		    if (!ctx.getSharedPreferences(Constants.PREFERENCESFILE,
				    Context.MODE_PRIVATE).getBoolean(Constants.OFFLINE_MODE,
				    false) && isconnected) {
		    	
				success = saveToServer(loc, oneGiPSData, false);
				if (success) {
					flushLocalStorageToServer(ctx.getSharedPreferences(Constants.PREFERENCESFILE,
							Context.MODE_PRIVATE).getBoolean(Constants.OFFLINE_MODE_NEVER_DELETE, false));
				}
		    } else {
				success = saveToLocalStorage(loc, oneGiPSData);
		    }
		}
		return success;
    }
    
    // Save location on Local Sqlite DB
	public boolean saveToLocalStorage(Location loc, GiPSExtraData oneGiPSData) {
		boolean success = false;
		
		// insert Location and GiPS data in DB
		gipsdb.beginTransaction();
		try {
			// get time of the observation
		    Calendar c = Calendar.getInstance();
		    c.setTimeZone(TimeZone.getDefault()); // ??????????????????????????
		    c.setTimeInMillis(loc.getTime());
		    
		    // prepare record to add
		    ContentValues cv = new ContentValues();
			cv.put("sent", 0);
			cv.put("acqdate", Utilities.getIsoDate(c));
			cv.put("lat", loc.getLatitude());
			cv.put("lon", loc.getLongitude());
			cv.put("altitude", loc.getAltitude());
			cv.put("bearing", loc.getBearing());
			cv.put("speed", loc.getSpeed());
			cv.put("accuracy", loc.getAccuracy());
			cv.put("provider", loc.getProvider());
			cv.put("key", oneGiPSData.key);
			cv.put("tag", oneGiPSData.tag);
			cv.put("buttoncode", oneGiPSData.buttoncode);
			
			// dump record to be saved in DB
			String msg = "Adding tuples:";
			Set<Entry<String, Object>> set = cv.valueSet();
			for (Entry<String, Object> element : set) {
				msg += " "+element.getKey()+":"+element.getValue();
			}
			Log.d(LOGTAG, msg);
			
			// save record
			gipsdb.insertOrThrow(Constants.TABLE_NAME, "", cv);
			gipsdb.setTransactionSuccessful();
			success = true;
			
		} catch(SQLException e) {
			Toast.makeText(ctx, "Error saving point in DB: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} finally {
			gipsdb.endTransaction();
		}
		return success;
    }
	
	// Save multiple location on Local DB
	public boolean saveToLocalStorage(List<Location> chunkToSaveToServer,
									List<GiPSExtraData> gipsChunkToSaveToServer) {
		// check if there are data
		if (chunkToSaveToServer.size() == 0 && gipsChunkToSaveToServer.size() == 0) return true;

		boolean success = false;
		
		for (int i = 0; i < chunkToSaveToServer.size(); i++) {
			success = saveToLocalStorage(chunkToSaveToServer.get(i), gipsChunkToSaveToServer.get(i));
			if (!success) break;
		}
		
		return success;		
	}

    /**
     * HTTP POST all the local storage to the server in a robust way. If it
     * fails, we'll try again next time...
     * ...And delete all flushed if required otherwise they are set as sent.
     * 
     * @return
     */
	public boolean flushLocalStorageToServer(boolean neverDelete) { 
    	
    	String sqlget;
    	String sqlupdate;
    	String sqlbutton;
    	
    	// get number of points to post together
	    int chunktosend = ctx.getSharedPreferences(Constants.PREFERENCESFILE,
			    Context.MODE_PRIVATE).getInt(Constants.CHUNK_DIMENSION, (Constants.MAX_LOCATIONS_PER_HTTP_POST - 1));
	    Log.d(LOGTAG, "Chunk dimension to POST to server = " + chunktosend);
	    
	    // !!! BEAWARE !!! 
	    // query will be done starting on button codes 
	    // due to fact that serverside CGI accept records 
	    // grouped by a uniform buttoncode (and key and tag)
	    boolean success = false;
		boolean flushedAllLocations = false;
		Cursor res = null;
		Cursor buttoncodes = null;
		Cursor resupdate = null;
		
		try {
		    // get all different buttoncode saved in DB
			sqlbutton = "SELECT DISTINCT " + Constants.DBCOLUMNS_buttoncode +
				" FROM " + Constants.TABLE_NAME +
				" WHERE (" + Constants.DBCOLUMNS_sent + "=0)" +
				" ORDER BY " + Constants.DBCOLUMNS_buttoncode;
			buttoncodes = gipsdb.rawQuery(sqlbutton, null);
			
			// check if empty otherwise... return successfully
			Log.d(LOGTAG, "Found "+buttoncodes.getCount()+" buttoncodes");
			if (buttoncodes.getCount() == 0) return success=true;
			
		    // prepare containers and loop on all buttoncodes
		    List<Location> chunkToSaveToServer = new ArrayList<Location>();
		    List<GiPSExtraData> gipsChunkToSaveToServer = new ArrayList<GiPSExtraData>();

			buttoncodes.moveToFirst();
			while (!buttoncodes.isAfterLast() && isconnected) {
				
				// get all record of a specific buttoncode looping on chunk to send
				Log.i(LOGTAG, "Look for not sent records with buttoncode = " + buttoncodes.getString(0));
				
				sqlget = "SELECT * FROM "+ Constants.TABLE_NAME + 
					" WHERE (" + Constants.DBCOLUMNS_sent + "=0)"+ 
					" AND (" + Constants.DBCOLUMNS_buttoncode + "='"+buttoncodes.getString(0)+"')"+
					" ORDER BY " + Constants.DBCOLUMNS_acqdate + 
					" LIMIT " + chunktosend;
				do {					
					if (res != null) {
						res.close();
						res = null;
					}

					Log.d(LOGTAG, "Get query: "+sqlget);
					res = gipsdb.rawQuery(sqlget, null);

					// check if empty
					if (res.getCount() == 0) {
						String txt = "Flushed all points on server with buttoncode = "+buttoncodes.getString(0);
						Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show();
						Log.i(LOGTAG, txt);

						flushedAllLocations = true;
						continue;
					}
					Log.i(LOGTAG, "Flushing next "+res.getCount()+" records");
					
					// empty containiers and fill them with element to send
					chunkToSaveToServer.clear();
					gipsChunkToSaveToServer.clear();
					
					Location loc = new Location("gipsdb");
					GiPSExtraData gipsdata = new GiPSExtraData();
					String sqlindexes = "";
					
					res.moveToFirst();
					while (!res.isAfterLast()) {
						
						// init data adding to container
						String datetimestring = res.getString(res.getColumnIndex(Constants.DBCOLUMNS_acqdate));
						Calendar calendar = Utilities.getCalendarFromISO(datetimestring);

						loc.reset();
						loc.setTime(		calendar.getTimeInMillis());
						loc.setLatitude(	res.getDouble(	res.getColumnIndex(Constants.DBCOLUMNS_lat)));
						loc.setLongitude(	res.getDouble(	res.getColumnIndex(Constants.DBCOLUMNS_lon)));
						loc.setAltitude(	res.getDouble(	res.getColumnIndex(Constants.DBCOLUMNS_altitude)));
						loc.setBearing(		res.getFloat(	res.getColumnIndex(Constants.DBCOLUMNS_bearing)));
						loc.setSpeed(		res.getFloat(	res.getColumnIndex(Constants.DBCOLUMNS_speed)));
						loc.setAccuracy(	res.getFloat(	res.getColumnIndex(Constants.DBCOLUMNS_accuracy)));
						loc.setProvider(	res.getString(	res.getColumnIndex(Constants.DBCOLUMNS_provider)));
						chunkToSaveToServer.add(loc);
						
						gipsdata.key = 		res.getString(	res.getColumnIndex(Constants.DBCOLUMNS_key));
						gipsdata.tag = 		res.getString(	res.getColumnIndex(Constants.DBCOLUMNS_tag));
						gipsdata.buttoncode = res.getString(res.getColumnIndex(Constants.DBCOLUMNS_buttoncode));
						gipsChunkToSaveToServer.add(gipsdata);
						
						// add index to update
						sqlindexes = sqlindexes + res.getString(0) + ",";
						
						res.moveToNext();
					}
					
					// cut last "," comma character
					sqlindexes = sqlindexes.substring(0, sqlindexes.length()-1);
					
					// POST data to server if connected
					if (!isconnected) continue;
					
				    if (!saveToServer(chunkToSaveToServer, gipsChunkToSaveToServer, true)) {
				    	String txt = "Error sending points";
						Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show();
						Log.e(LOGTAG, txt);
							
						success = false;
						return success;
				    }
					Log.d(LOGTAG, "Indexes sent to server = "+sqlindexes);
				    
					// if set, delete sent points otherwise set flag to sent
					gipsdb.beginTransaction();
					{
						if (resupdate != null) {
							resupdate.close();
							resupdate = null;
						}

						if (neverDelete) {
						    sqlupdate = "UPDATE " + Constants.TABLE_NAME + 
					    		" SET " + Constants.DBCOLUMNS_sent + "=1";
						} else {
							sqlupdate = "DELETE FROM " + Constants.TABLE_NAME;
						}
						sqlupdate += " WHERE _id IN (" + sqlindexes + ")";
						
						Log.d(LOGTAG, "Update query: "+sqlupdate);
						resupdate = gipsdb.rawQuery(sqlupdate, null);
						Log.d(LOGTAG, "Update DONE! "+resupdate.getCount());
					}
					gipsdb.setTransactionSuccessful();
					gipsdb.endTransaction();

				} while (!flushedAllLocations && isconnected) ;
				
				// get new set of records basing on the next buttoncode
				buttoncodes.moveToNext();
				
			} // while buttoncodes
			success = true;

		} catch (SQLException e) {
			String txt = "Error getting points from DB: "+e.getLocalizedMessage();
			Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show();
			Log.e(LOGTAG, txt);
			
			e.printStackTrace();
		} finally {
			if (res != null) {
				res.close();
				res = null;
			}
			if (buttoncodes != null) {
				buttoncodes.close();
				buttoncodes = null;
			}
			if (resupdate != null) {
				resupdate.close();
				resupdate = null;
			}
			if (gipsdb.inTransaction()) gipsdb.endTransaction();
        }
		return success;
	}

    /**
     * Save all these chunks to the server in one big HTTP POST
     * 
     * @param chunkToSaveToServer
     * @param gipsChunkToSaveToServer
     * @param archived
     * 			boolean to notify if points to sent comes from DB buffer or not
     * @return false if this failed
     */
    public boolean saveToServer(List<Location> chunkToSaveToServer,
    							List<GiPSExtraData> gipsChunkToSaveToServer,
    							boolean archived) {
    	// check if there are data
    	if (chunkToSaveToServer.size() == 0 && gipsChunkToSaveToServer.size() == 0) return true;
    	
		boolean success = false;
		HttpURLConnection connection = null;
		SharedPreferences mPrefs = ctx.getSharedPreferences(Constants.PREFERENCESFILE, Context.MODE_PRIVATE);
		boolean vibratemode = mPrefs.getBoolean(Constants.VIBRATE_MODE, false);
		try {
		    URL postUrl = new URL(url + Constants.DEFAULT_SERVICE);// Assumes url ends
		    // with "/"
		    URLConnection uConnection = postUrl.openConnection();
		    if (uConnection instanceof HttpURLConnection) {
				// build parameter list
				String parameters = addIfFilled("key", gipsChunkToSaveToServer.get(0).key)
					+ addIfFilled("tag", gipsChunkToSaveToServer.get(0).tag)
					+ addIfFilled("buttoncode",  gipsChunkToSaveToServer.get(0).buttoncode)
					+ addIfFilled("ndata", Integer.valueOf(
						chunkToSaveToServer.size()).toString())
					+ locationsToUrlArguments(chunkToSaveToServer);
		
				connection = (HttpURLConnection) uConnection;
				connection.setReadTimeout(10000); // 30 seconds
				connection.setConnectTimeout(10000);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", ""
					+ Integer.toString(parameters.getBytes().length));
				connection.setDoOutput(true); // write data to the server
				connection.setDoInput(true); // read data from the server
				connection.setUseCaches(false); // server access to the latest
				// information
				connection.setInstanceFollowRedirects(true);
				connection.setAllowUserInteraction(false);
		
				Log.d(LOGTAG, "Submitting chunk of " + chunkToSaveToServer.size()
					+ " locations to " + connection.getURL());
		
				// Send request
				DataOutputStream wr = new DataOutputStream(connection
					.getOutputStream());
				wr.writeBytes(parameters);
				wr.flush();
				wr.close();
		
				// process the respose
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				    success = true;
				    // read the result from the server
				    BufferedReader rd = new BufferedReader(
					    new InputStreamReader(connection.getInputStream()),
					    8192);
				    String line = null;
				    StringBuilder sb = new StringBuilder();
		
				    while ((line = rd.readLine()) != null) {
				    	sb.append(line + '\n');
				    }
				    Log.d(LOGTAG, "Success HTTP POST response = " + connection.getResponseCode());
						    // + connection.getResponseMessage());
				    //Log.d(LOGTAG, "HTTP POST returned: " + sb.toString());
				    
				    // notify vibrating
					if (vibratemode) {
						int vibinterval = mPrefs.getInt(Constants.VIBRATE_INTERVAL, Constants.VIBRATE_DEFAULT_INTERVAL);
						vibrator.vibrate(vibinterval);
					}
				} else {
				    Log.w(LOGTAG, "HTTP POST failed with code "
					    + connection.getResponseCode() + ": "
					    + connection.getResponseMessage());
				    // vibrate to show fail
					if (vibratemode) vibrator.vibrate(new long[]{50, 25, 50, 25, 50}, -1);
				}
		    }
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
			
			// save points in DB because saving on server has failed
		    // and thery are not already archived
		    if (!archived) saveToLocalStorage(chunkToSaveToServer, gipsChunkToSaveToServer);
			
		} finally {
		    if (connection != null)	connection.disconnect();
		}
		return success;
    }

    /**
     * Little convenience method
     * 
     * @param locationToSaveToServer
     * @param oneGiPSData
     * @param archived
     * 			boolean to notify if points to sent comes from DB buffer or not
     * @return false if it failed
     */
    public boolean saveToServer(Location locationToSaveToServer, GiPSExtraData oneGiPSData, boolean archived) {
		List<Location> oneLocation = new ArrayList<Location>();
		oneLocation.add(locationToSaveToServer);
		
		List<GiPSExtraData> oneGiPSDatas = new ArrayList<GiPSExtraData>();
		oneGiPSDatas.add(oneGiPSData);
		
		return saveToServer(oneLocation, oneGiPSDatas, archived);
    }

    public String locationsToUrlArguments(List<Location> chunkToSaveToServer) {
		StringBuilder sb = new StringBuilder();
		// add all points
		for (int pointCounter = 0; pointCounter < chunkToSaveToServer.size(); pointCounter++) {
		    Location toAdd = chunkToSaveToServer.get(pointCounter);
		    // convert date to string format
		    Calendar c = Calendar.getInstance();
		    c.setTimeZone(TimeZone.getDefault());
		    c.setTimeInMillis(toAdd.getTime());
		    sb.append(addIfFilled("time" + (pointCounter + 1), Utilities
			    .getIsoDate(c)));
		    // it would be handy to use seconds since epoch, but johan asked for
		    // ISO 8xxx dates so we do that
		    // sb.append(addIfFilled("time" + (pointCounter + 1), ""
		    // + toAdd.getTime() / 1000));
		    // add other parameters
		    
		    sb.append(
			    addIfFilled("latitude" + (pointCounter + 1), df
				    .format(toAdd.getLatitude()))).append(
			    addIfFilled("longitude" + (pointCounter + 1), df
				    .format(toAdd.getLongitude()))).append(
			    addIfFilled("provider" + (pointCounter + 1), toAdd
				    .getProvider()));
	
		    if (toAdd.hasAltitude())
			sb.append(addIfFilled("altitude" + (pointCounter + 1), df
				.format(toAdd.getAltitude())));
		    if (toAdd.hasSpeed())
			sb.append(addIfFilled("speed" + (pointCounter + 1), df
				.format(toAdd.getSpeed())));
		    if (toAdd.hasBearing())
			sb.append(addIfFilled("bearing" + (pointCounter + 1), df
				.format(toAdd.getBearing())));
		    if (toAdd.hasAccuracy())
			sb.append(addIfFilled("accuracy" + (pointCounter + 1), df
				.format(toAdd.getAccuracy())));
		}
		//System.out.println(sb.toString());
		return sb.toString();
    }

    private String addIfFilled(String key, String value) 
    {
	    if (key != null && value != null && !key.equals("") && !value.equals("")) 
	    {
	        return "&" + key + "=" + URLEncoder.encode(value);
	    } 
	    else 
	    {
	        return "";
	    }
    }

    public void stopRunning() {
		this.isRunning = false;
		this.lastlat = 0.0d;
		this.lastlng = 0.0d;
		this.lastdist = 0.0d;
    }

    @Override
    public void onLocationChanged(Location loc) {
		if (loc != null) {
		    Date now = new Date();
		    if (lastTime == null
			    || now.getTime() / 1000L - lastTime.getTime() / 1000L > interval) {
		    	
		    	// save location
				GiPSExtraData oneGiPSData = new GiPSExtraData();
				oneGiPSData.key = key;
				oneGiPSData.tag = tag;
				oneGiPSData.buttoncode = buttoncode;		

				boolean res = saveLocation(loc, oneGiPSData);
		
				if (onePoint) {
				    if (res) {
						Toast.makeText(ctx, "Point sent", Toast.LENGTH_SHORT).show();
				    } else {
						Toast.makeText(ctx, "Failed sending point",	Toast.LENGTH_SHORT).show();
				    }
				    Log.d(LOGTAG, "Disabling location updates for sendPoint");
				    lm.removeUpdates(this);
				    try {
					    ctx.unregisterReceiver(connReceiver);
				    } catch (Exception e) {}
				} else {
				    double thislat = loc.getLatitude();
				    double thislng = loc.getLongitude();
		
				    if (lastlat != 0.0d || lastlng != 0.0d) {
						double currdist = Utilities.calcDistance(lastlat,
							lastlng, thislat, thislng);
						lastdist += (isMetric) ? currdist
							: currdist * 0.621371192d;
				    }
		
				    if (activity != null) 
				    {
				        String mode="online";	
				    
					    if (ctx.getSharedPreferences(Constants.PREFERENCESFILE,
							    Context.MODE_PRIVATE).getBoolean(Constants.OFFLINE_MODE,
							    false)) 
					    {
					    	mode="offline";
					    }
				    	
				    	((TextView) activity.findViewById(R.id.distanceValue))
							.setText(new DecimalFormat("#.###").format(lastdist) + " " + unit + " ("+mode+")");
				    } else {
				    	Log.d(LOGTAG,"activity == null, so we cannot set the distance travelled :-(");
				    }
		
				    lastlat = thislat;
				    lastlng = thislng;
		
				}
				lastTime = now;
		    }
		} else {
		    Log.w(LOGTAG, "Location object loc == null, we have no location !");
		}
    }

    @Override
    public void onProviderDisabled(String provider) {
    	lm.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void run() {
		while (isRunning) {
		    try {
		    	Thread.sleep(100);
		    } catch (Exception e) {
	
		    }
		}
    }
}