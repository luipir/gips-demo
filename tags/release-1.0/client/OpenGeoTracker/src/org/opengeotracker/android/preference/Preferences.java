package org.opengeotracker.android.preference;

import org.opengeotracker.android.Constants;
import org.opengeotracker.android.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);
    }

    /**
     * Normally, this Activity uses getDefaultSharedPreferences, but since the
     * legacy users have their preferences in custom-named SharedPreferences, we
     * override this here.
     */
    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
	return super.getSharedPreferences(Constants.PREFERENCESFILE,
		MODE_PRIVATE);
    }

    @Override
    public SharedPreferences getPreferences(int mode) {
	return super.getSharedPreferences(Constants.PREFERENCESFILE,
		MODE_PRIVATE);
    }

}