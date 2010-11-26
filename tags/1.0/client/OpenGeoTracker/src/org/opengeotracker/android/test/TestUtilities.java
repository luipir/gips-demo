package org.opengeotracker.android.test;

import junit.framework.TestCase;

import org.opengeotracker.android.Utilities;

public class TestUtilities extends TestCase {

    // this test may fail on other machines because of time zone differences
    // so please make it more generic if it fails...
    public void testToISOdate() {
	assertEquals("2010-08-27T15:00:00.00+01:00", Utilities
		.getIsoDate(TestConstants.getTestCalendar()));
    }

    public void testLocationToString() {
	assertEquals(TestConstants.TESTLOCATION, Utilities
		.locationToString(TestConstants.getTestLocation()));
    }

    public void testStringToLocation() {
	assertTrue(Utilities.locationEquals(TestConstants.getTestLocation(),
		Utilities.stringToLocation(TestConstants.TESTLOCATION)));
    }

}
