package fi.salminen.gpsmap;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
 * http://stackoverflow.com/questions/8828639/android-get-gps-location-via-a-service
 */
public class DatabaseService extends Service
{
	private static final String TAG = "DatabaseService";
	
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
	private PlacesDBAdapter mDbAdapter = null;

	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class LocalBinder extends Binder {
		DatabaseService getService() {
			Log.i(TAG, "LocalBinder.getService");
			return DatabaseService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return mBinder;
	}
	
	@Override
	public void onCreate()
	{
		Log.i(TAG, "onCreate");
		initializeDbAdapter();
		mDbAdapter.open();
	}

	@Override
	public void onDestroy()
	{
		Log.e(TAG, "onDestroy");
		if (mDbAdapter != null) {
			mDbAdapter.close();
		}
		super.onDestroy();
	} 

	private void initializeDbAdapter() {
		Log.e(TAG, "initializeDbAdapter");
//		if (mDbAdapter == null) {
			mDbAdapter = new PlacesDBAdapter(this);
//		}
	}

	public long createPlace(Location loc)
	{	
		if (mDbAdapter != null) {
			return mDbAdapter.createPlace(loc);
		} else {
			Log.e(TAG, "createPlace");
			return 0L;
		}
	}

	public boolean deletePlace(String rowId)
	{
		return mDbAdapter.deletePlace(rowId);
	}
	
	public boolean deleteAllPlaces() {
		return mDbAdapter.deleteAllPlaces();
	}

	public Cursor fetchAllPlaces() {
		return mDbAdapter.fetchAllPlaces();
	}

	public Cursor fetchPlace(String rowId) throws SQLException {
		return mDbAdapter.fetchPlace(rowId);
	}
}