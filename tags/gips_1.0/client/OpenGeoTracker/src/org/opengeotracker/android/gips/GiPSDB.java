package org.opengeotracker.android.gips;

import org.opengeotracker.android.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class GiPSDB extends SQLiteOpenHelper {
	
	private static final String LOGTAG="GiPS_DbHelper";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_CREATE =
                "CREATE TABLE " + Constants.TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.DBCOLUMNS_sent 		+" INTEGER, " +
                Constants.DBCOLUMNS_acqdate 	+" TEXT, " +
                Constants.DBCOLUMNS_lat 		+" REAL, " +
                Constants.DBCOLUMNS_lon 		+" REAL, " +
                Constants.DBCOLUMNS_altitude	+" REAL, " +
                Constants.DBCOLUMNS_bearing 	+" REAL, " +
                Constants.DBCOLUMNS_speed 		+" REAL, " +
                Constants.DBCOLUMNS_accuracy 	+" REAL, " +
                Constants.DBCOLUMNS_provider 	+" TEXT, " +
                Constants.DBCOLUMNS_key 		+" TEXT, " +
                Constants.DBCOLUMNS_tag 		+" TEXT, " +
                Constants.DBCOLUMNS_buttoncode 	+" TEXT);";
    
    public GiPSDB(Context context, String name, CursorFactory factory, int version) {
		super(context, Constants.DBNAME, null, DATABASE_VERSION);
	}

    @Override
    public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOGTAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        onCreate(db);
	}
}
