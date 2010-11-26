package org.opengeotracker.android.gips;

import org.opengeotracker.android.Constants;
import org.opengeotracker.android.LocationListenerHolder;
import org.opengeotracker.android.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Observation extends Activity {

	String LOGTAG;
	Vibrator vibrator;
	SharedPreferences mPrefs;
	boolean vibratemode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.observation);
		
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mPrefs = getSharedPreferences(Constants.PREFERENCESFILE, Context.MODE_PRIVATE);
		vibratemode = mPrefs.getBoolean(Constants.VIBRATE_MODE, false);
		
		LOGTAG = this.toString();
		
		((Button) findViewById(R.id.BtnDistributed)).setOnClickListener(new onClickListener());
		((Button) findViewById(R.id.BtnDistributedMultiple)).setOnClickListener(new onClickListener());
		((Button) findViewById(R.id.BtnBoxBlocked)).setOnClickListener(new onClickListener());
		((Button) findViewById(R.id.BtnUnavailable)).setOnClickListener(new onClickListener());
	}

	class onClickListener implements OnClickListener {
		@Override
	    public void onClick(View view) {
			// vibrate?
			if (vibratemode) {
				Log.i(LOGTAG, "VIbRaTe.................");
			}
			vibrator.vibrate(40);
			
	    	// get tag to recognise button
	    	String buttoncode = (String) view.getTag();
			Log.i(LOGTAG, "Pressed button with code = "+buttoncode);
	    	
	    	// get extra data to send to logging
	    	Bundle extradata = getIntent().getBundleExtra("extradata");
	    	
	    	Log.i(LOGTAG, "key = "+extradata.getString("key"));
	    	Log.i(LOGTAG, "url = "+extradata.getString("url"));
	    	Log.i(LOGTAG, "tag = "+extradata.getString("tag"));
	    	
			LocationListenerHolder.pointLogging(
					view.getContext(), 
					extradata.getString("key"),
					extradata.getInt("nint"), 
					extradata.getString("url"), 
					extradata.getString("tag"),
					buttoncode);
	    }
	}
}