package fi.salminen.gpsmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PlaceListDB extends SQLiteOpenHelper {
	private static final String TAG = "PlaceListDB";

	public static final String KEY_NAME = "name";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_ROWID = "_id";

	public static final String DATABASE_NAME = "data";
	public static final String DATABASE_TABLE = "places";
	private static final int DATABASE_VERSION = 2;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
			"CREATE TABLE " + DATABASE_TABLE + "("
					+ KEY_ROWID + " integer primary key autoincrement, "
					+ KEY_NAME + " text not null,"
					+ KEY_LATITUDE + " text not null,"
					+ KEY_LONGITUDE + " text not null"
					+ ");";


	public PlaceListDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database. Existing contents will be lost. ["
				+ oldVersion + "]->[" + newVersion + "]");
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		onCreate(db);	}
}
