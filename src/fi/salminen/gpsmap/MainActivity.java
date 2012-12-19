package fi.salminen.gpsmap;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import fi.salminen.gpsmap.R.id;
import fi.salminen.gpsmap.R.string;

public class MainActivity extends FragmentActivity implements PlaceListFrag.OnPlaceSelectedListener {
	private static final String TAG = "MainActivity";
	
	static final String ZOOM = "zoom";
	static final String LATITUDE = "latitude";
	static final String LONGITUDE = "longitude";
	static final String MESSAGE = "message";
	static final String PREV_LATITUDE = "prev_latitude";
	static final String PREV_LONGITUDE = "prev_longitude";
	static final String UPDATE_TIME_INTERVAL = "update_time_interval";
	static final String UPDATE_TRAVEL_DISTANCE = "update_travel_distance";
	static final int NOTIFICATION_ID = 1234;
	
	private LocationService mLocationService;
	private boolean mLocationServiceIsBound;
	private Timer timer = new Timer();
	private long update_time_interval = 60000L;
	private float update_travel_distance = 10;
	private float zoom = 15;
	private String message;
	private double latitude = 0, longitude = 0, prev_latitude = 0, prev_longitude = 0;
	
	private MapFrag mapFrag = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");

		this.setContentView(R.layout.activity_main);

		createNotification();
		
		// Check whether we're recreating a previously destroyed instance
	    if (savedInstanceState != null) {
	        // Restore value of members from saved state
			zoom = savedInstanceState.getFloat(ZOOM);
			latitude = savedInstanceState.getDouble(LATITUDE);
			longitude = savedInstanceState.getDouble(LONGITUDE);
			message = savedInstanceState.getString(MESSAGE);
			prev_latitude = savedInstanceState.getDouble(PREV_LATITUDE);
			prev_longitude = savedInstanceState.getDouble(PREV_LONGITUDE);
			update_time_interval = savedInstanceState.getLong(UPDATE_TIME_INTERVAL);
			update_travel_distance = savedInstanceState.getFloat(UPDATE_TRAVEL_DISTANCE);
	    }
		
		// Check if GPS is enabled. Nothing to do with LocationService.
		LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		if (!enabled) {
			showGPSDisabledAlertToUser();
		} 


		FragmentManager fragmentManager = this.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first fragment
		if (findViewById(R.id.fragment_container) != null) {
			if(fragmentManager.findFragmentById(id.fragment_container) == null) {
				PlaceListFrag placeListFrag = new PlaceListFrag();
				placeListFrag.setArguments(createBundle());
				fragmentTransaction.add(R.id.fragment_container, placeListFrag);
				fragmentTransaction.commit();
			}
		} else {		
			// If there are fragment in one of the two frame layouts then both must be there.
			fragmentManager.popBackStack(); // Tämän poistaa ylimääräisen fragmentin stackista. Ei tarvitse painaa kahta kertaa backia sovelluksesta poistumiseen.
			mapFrag = (MapFrag) fragmentManager.findFragmentById(id.fragment_mapfrag);
			if (mapFrag == null) {
				mapFrag = new MapFrag();
				PlaceListFrag placeListFrag = new PlaceListFrag();
				fragmentTransaction.add(R.id.fragment_placelistfrag, placeListFrag);
				fragmentTransaction.add(R.id.fragment_mapfrag, mapFrag);
				fragmentTransaction.commit();
			}			
			mapFrag.setLocation(createBundle());
		}
	}

	private void createNotification() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_stat_gpsmap)
		        .setContentTitle("GPSMap")
		        .setContentText("Saving locations...");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = mBuilder.build();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	private Bundle createBundle() {
		Bundle bundle = new Bundle();
		bundle.putFloat(ZOOM, zoom);
		bundle.putDouble(LATITUDE, latitude);
		bundle.putDouble(LONGITUDE, longitude);
		bundle.putString(MESSAGE, message);
		return bundle;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onSaveInstance");
	    // Save current state
	    savedInstanceState.putFloat(ZOOM, zoom);
	    savedInstanceState.putDouble(LATITUDE, latitude);
	    savedInstanceState.putDouble(LONGITUDE, longitude);
	    savedInstanceState.putString(MESSAGE, message);
	    savedInstanceState.putDouble(PREV_LATITUDE, prev_latitude);
	    savedInstanceState.putDouble(PREV_LONGITUDE, prev_longitude);
	    savedInstanceState.putLong(UPDATE_TIME_INTERVAL, update_time_interval);
	    savedInstanceState.putFloat(UPDATE_TRAVEL_DISTANCE, update_travel_distance);
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case id.remove_all_places:
			removeAllPlaces();
			return true;
		case id.seconds15:
			update_time_interval = 1000 * 15;
			startTimer();
			return true;
		case id.minute1:
			update_time_interval = 1000 * 60 * 1;
			startTimer();
			return true;
		case id.minutes2:
			update_time_interval = 1000 * 60 * 2;
			startTimer();
			return true;
		case id.minutes5:
			update_time_interval = 1000 * 60 * 5;
			startTimer();
			return true;
		case id.minutes10:
			update_time_interval = 1000 * 60 * 10;
			startTimer();
			return true;
		case id.meter1:
			update_travel_distance = 1;
			return true;
		case id.meters10:
			update_travel_distance = 10;
			return true;
		case id.meters50:
			update_travel_distance = 50;
			return true;
		case id.meters100:
			update_travel_distance = 100;
			return true;
		case id.meters500:
			update_travel_distance = 500;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindLocationService();		
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (timer != null) {timer.cancel();}
		doUnbindLocationService();		
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
			mLocationService = binder.getService();
			mLocationServiceIsBound = true;			
			startTimer();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mLocationServiceIsBound = false;
			timer.cancel();
		}
	};

	private void doBindLocationService() {
		// Establish a connection with the service.  We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		Intent locationServiceIntent = new Intent(this, LocationService.class);
		mLocationServiceIsBound = bindService(locationServiceIntent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindLocationService() {
		if (mLocationServiceIsBound) {
			// Detach our existing connection.
			unbindService(mLocationServiceConnection);
			mLocationServiceIsBound = false;
		}
	}

	/*
	 * http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
	 */
	private void showGPSDisabledAlertToUser(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(string.enable_GPS)
		.setCancelable(false)
		.setPositiveButton(string.goto_GPS_settings,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				Intent callGPSSettingIntent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(callGPSSettingIntent);
			}
		});
		alertDialogBuilder.setNegativeButton(string.cancel,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	@Override
	public void onPlaceSelected(String rowID) {
		String[] projection = { PlaceListDB.KEY_ROWID, PlaceListDB.KEY_NAME, PlaceListDB.KEY_LATITUDE, PlaceListDB.KEY_LONGITUDE };
		String selection = PlaceListDB.KEY_ROWID + "=" + rowID;
		Cursor c = getContentResolver().query(PlaceListProvider.CONTENT_URI, projection, selection, null, null);
		c.moveToFirst();
		message = c.getString(c.getColumnIndex(PlaceListDB.KEY_NAME));
		latitude = Double.parseDouble(c.getString(c.getColumnIndex(PlaceListDB.KEY_LATITUDE)));
		longitude = Double.parseDouble(c.getString(c.getColumnIndex(PlaceListDB.KEY_LONGITUDE)));
		Log.i(TAG, "onPlaceSelected: " + latitude + ", " + longitude + ", " + message);
		c.close();
		
		FragmentManager fragmentManager = this.getSupportFragmentManager();

		if (findViewById(id.fragment_container) != null) {
			// portrait - 1 fragment
			Log.i(TAG, "Vaihda containerin sisältö mapFragiin.");

			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

			if (mapFrag == null) {
				mapFrag = new MapFrag();
			} 

			mapFrag.setLocation(createBundle());

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			fragmentTransaction.replace(R.id.fragment_container, mapFrag);
			fragmentTransaction.addToBackStack(null);

			// Commit the transaction
			fragmentTransaction.commit();

		} else {
			// landscape - 2 fragments
			Log.i(TAG, "Päivitä mapFrag.");
			// If map frag is available, we're in two-pane layout...
			
			MapFrag mFrag = (MapFrag) fragmentManager.findFragmentById(id.fragment_mapfrag);
			// Call a method in the MapFrag to update its content
			mFrag.setLocation(createBundle());
			mFrag.updateMapMarker();
		}
	}
	
	private void startTimer() {
		timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 10000L, update_time_interval);
	}

	private void onTimerTick() {
		Log.i(TAG, "onTimerTick");
		if (mLocationServiceIsBound) {
			try {
				Location currentGPSLocation = mLocationService.getLastGPSLocation();
				// Create place only if location is valid.
				float traveled_distance = 0;
				if (currentGPSLocation.getAccuracy() > 0) {
					// if previous location is null then always create place else check if travelled distance is acceptable.
					if ((prev_latitude == 0 && prev_longitude == 0) || ((traveled_distance = traveledDistance(currentGPSLocation)) > update_travel_distance)) {
						Log.i(TAG, "Traveled: " + traveled_distance + "m");
						addPlace(currentGPSLocation);
						prev_latitude = currentGPSLocation.getLatitude();
						prev_longitude = currentGPSLocation.getLongitude();
					} else {
						Log.i(TAG, "Not traveled enough! " + traveled_distance + "m" ) ;
					}
				}
			} catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
				Log.e(TAG, "onTimerTick failed.", t);            
			}
		} else {
			Log.e(TAG, "Location service not available");
		}
	}

	private void addPlace(Location loc) {
		Time time = new Time(Time.getCurrentTimezone());
		time.set(loc.getTime());
		ContentValues placeData = new ContentValues();
		placeData.put(
		        PlaceListDB.KEY_NAME,
		        time.format("%k:%M:%S"));
		placeData.put(
		        PlaceListDB.KEY_LATITUDE,
		        Double.toString(loc.getLatitude()));
		placeData.put(
		        PlaceListDB.KEY_LONGITUDE,
		        Double.toString(loc.getLongitude()));
		getContentResolver().insert(
		        PlaceListProvider.CONTENT_URI,
		        placeData);
	}
	
	private void removeAllPlaces() {
		getContentResolver().delete(PlaceListProvider.CONTENT_URI, null, null);
	}
	
	private float traveledDistance(Location locB) {
		Location locA = new Location("Point A");
		locA.setLatitude(prev_latitude);
		locA.setLongitude(prev_longitude);
		
		return locA.distanceTo(locB);
	}
}
