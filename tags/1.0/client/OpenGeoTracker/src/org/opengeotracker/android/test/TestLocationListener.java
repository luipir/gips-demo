package org.opengeotracker.android.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.opengeotracker.android.Constants;
import org.opengeotracker.android.GeoLocationListener;
import org.opengeotracker.android.OpenGeoTracker;
import org.opengeotracker.android.Utilities;
import org.opengeotracker.android.gips.GiPSExtraData;

import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;

/**
 * This test class can be used to test various aspects of the project.
 */
public class TestLocationListener extends
	ActivityInstrumentationTestCase2<OpenGeoTracker> {

    private GeoLocationListener locListener;
    private OpenGeoTracker mainActivity;

    public void testSaveToServer() {
	// Note: we're only testing if the server returns 200, not if it was
	// actually saved to database
	assertTrue(locListener.saveToServer(TestConstants.getTestLocation(), TestConstants.getTestGipsdata(), false));
    }
    
    public void testLocationsToUrlArguments() {
    	List<Location> ll = new ArrayList<Location>();
    	ll.add(TestConstants.getTestLocation());
    	String res = locListener.locationsToUrlArguments(ll);
    	assertEquals("Inncorrect URL",TestConstants.TESTURL,res);
    	
    }

    public void testSaveToLocalStorage() {
	File tmpFile = null;
	try {
	    tmpFile = File.createTempFile("testSaveToLocalStorage", ".csv");
	    //locListener.saveToLocalStorage(TestConstants.getTestLocation(),
		//	    tmpFile.getCanonicalPath());
	    locListener.saveToLocalStorage(TestConstants.getTestLocation(), TestConstants.getTestGipsdata());
	    assertEquals(TestConstants.TESTLOCATION, Utilities.fileToString(tmpFile));
	} catch (IOException e) {
	    fail("Error creating temp file for testing local storage: "
		    + e.getLocalizedMessage());
	} finally {
	    if (tmpFile != null)
		tmpFile.delete();
	}
    }

    public void testSaveChunkToServer() {
    	ArrayList<Location> chunk = new ArrayList<Location>();
    	ArrayList<GiPSExtraData> gipschunks = new ArrayList<GiPSExtraData>();
	for (int i = 0; i < Constants.MAX_LOCATIONS_PER_HTTP_POST; i++) {
	    Location locationToSaveToServer = new Location(
		    "testSaveChunkToServer" + i);
	    Calendar now = Calendar.getInstance();
	    locationToSaveToServer.setTime(now.getTimeInMillis());
	    locationToSaveToServer.setLatitude(11.11);
	    locationToSaveToServer.setLongitude(22.123456789012);
	    locationToSaveToServer.setProvider("gps" + i);
	    locationToSaveToServer.setAccuracy(Float.valueOf("55.11"));
	    locationToSaveToServer.setAltitude(i);
	    locationToSaveToServer.setBearing(Float.valueOf("123.12"));
	    locationToSaveToServer.setSpeed(Float.valueOf("222.22"));
	    chunk.add(locationToSaveToServer);
	    
	    GiPSExtraData gipschunk = new GiPSExtraData();
	    gipschunk.key = "gipskey";
	    gipschunk.tag = "gipstag";
	    gipschunk.buttoncode = "gipsbuttoncode";
	   	gipschunks.add(gipschunk);
	}
	assertTrue(locListener.saveToServer(chunk, gipschunks, false));
    }

    public void testFlushLocalStorageToServer() {
	File tmpFile = null;
	try {
	    tmpFile = File.createTempFile("testSaveToLocalStorage", ".csv");
	    // write a lot of locations to local storage
	    for (int i = 0; i < (Constants.MAX_LOCATIONS_PER_HTTP_POST * 2 + 2); i++) {
		Location locationToSaveToServer = new Location(
			"testFlushLocalStorageToServer" + i);
		Calendar now = Calendar.getInstance();
		locationToSaveToServer.setTime(now.getTimeInMillis());
		locationToSaveToServer.setLatitude(11.11);
		locationToSaveToServer.setLongitude(22.123456789012);
		locationToSaveToServer.setProvider("gps" + i);
		locationToSaveToServer.setAccuracy(Float.valueOf("55.11"));
		locationToSaveToServer.setAltitude(i);
		locationToSaveToServer.setBearing(Float.valueOf("123.12"));
		locationToSaveToServer.setSpeed(Float.valueOf("222.22"));
		//locListener.saveToLocalStorage(locationToSaveToServer,
		//		absFileName);
    	GiPSExtraData oneGiPSData = new GiPSExtraData();
    	oneGiPSData.key = "key";
    	oneGiPSData.tag = "tag";
    	oneGiPSData.buttoncode = "buttoncode";

    	locListener.saveToLocalStorage(locationToSaveToServer, oneGiPSData);
	    }
	    // flush local storage to server
	    assertTrue(locListener.flushLocalStorageToServer(true));
	    assertFalse("The local location cache file should be gone or renamed after flushing", tmpFile.exists());	    
	} catch (IOException e) {
	    fail("Error creating temp file for testing local storage: "
		    + e.getLocalizedMessage());
	} finally {
	    if (tmpFile != null)
		tmpFile.delete();
	}

    }

  

    // ActivityInstrumentationTestCase2 plumbing:
    // ******************************************
    @Override
    protected void setUp() throws Exception {
	super.setUp();
	mainActivity = this.getActivity();
	locListener = new GeoLocationListener(mainActivity, null, null, this
		.getName()
		+ TestConstants.KEY, 30, TestConstants.SERVER_URL, this.getName()
		+ TestConstants.TAG, "metric", true);

	// as you can see, we can access the gui from here for later testing:
	// Button b = ((Button) mainActivity.findViewById(R.id.points));
    }

    public TestLocationListener() {
	super("org.opengeotracker.android", OpenGeoTracker.class);
    }    

}
