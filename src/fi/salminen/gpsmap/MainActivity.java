package fi.salminen.gpsmap;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import fi.salminen.gpsmap.R.id;
import fi.salminen.gpsmap.R.string;

public class MainActivity extends FragmentActivity implements PlaceListFrag.OnPlaceSelectedListener {
	private static final String TAG = "MainActivity";
	private LocationService mLocationService;
	private boolean mLocationServiceIsBound;
	private Timer timer = new Timer();
	private DatabaseService mDatabaseService;
	private boolean mDatabaseServiceIsBound;
	private long update_time_interval = 60000L;
	private double update_travel_distance = 10;
	private Location previousLocation = null;
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	doBindDatabaseService();
    	doBindLocationService();

    	LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		if (!enabled) {
			showGPSDisabledAlertToUser();
		} 
        
    	setContentView(R.layout.activity_main);

        
        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of ExampleFragment
            PlaceListFrag firstFragment = new PlaceListFrag();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }
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
                mDatabaseService.deleteAllPlaces();
                updatePlaceList();
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
    protected void onStart() {
    	super.onStart();
    }
	
    @Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer != null) {timer.cancel();}
		doUnbindLocationService();
		doUnbindDatabaseService();
	}

    private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mLocationService = ((LocalBinder<LocationService>) service).getService();
//	        mLocationService = ((LocationService.LocalBinder) service).getService();
	        startTimer();
	    }

		@Override
		public void onServiceDisconnected(ComponentName name) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
			// see this happen.
			mLocationService = null;
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

    private ServiceConnection mDatabaseServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
	        mDatabaseService = ((LocalBinder<DatabaseService>) service).getService();
	    }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDatabaseService = null;
		}
    };
    
    private void doBindDatabaseService() {
    	Intent databaseServiceIntent = new Intent(this, DatabaseService.class);
    	mDatabaseServiceIsBound = bindService(databaseServiceIntent, mDatabaseServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindDatabaseService() {
    	if (mDatabaseServiceIsBound) {
    		// Detach our existing connection.
    		unbindService(mDatabaseServiceConnection);
    		mDatabaseServiceIsBound = false;
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
		Cursor c = mDatabaseService.fetchPlace(rowID);
		String latitude = c.getString(c.getColumnIndex(PlacesDBAdapter.KEY_LATITUDE));
		String longitude = c.getString(c.getColumnIndex(PlacesDBAdapter.KEY_LONGITUDE));
		c.close();

        // Capture the article fragment from the activity layout
        MapFrag frag = (MapFrag)
                getSupportFragmentManager().findFragmentById(R.id.fragment_mapfrag);

        if (frag != null) {
            // If map frag is available, we're in two-panel layout...

            // Call a method in the MapFrag to update its content
            frag.updateMapMarker(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            MapFrag newFrag = new MapFrag();
			Bundle args = new Bundle();
            args.putDouble(MapFrag.ARG_LAT, Double.parseDouble(latitude));
            args.putDouble(MapFrag.ARG_LON, Double.parseDouble(longitude));
            args.putFloat(MapFrag.ARG_ZOOM, 15);
            newFrag.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFrag);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

        }
	}
	
	private void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 10000L, update_time_interval);
	}
	
	private void onTimerTick() {
		Log.i(TAG, "onTimerTick");
		try {
			Location currentGPSLocation = mLocationService.getLastGPSLocation();
			
			// Create place only if location is valid.
			if (currentGPSLocation.getAccuracy() > 0) {
				// if previous location is null then always create place else check if travelled distance is acceptable.
				float traveledDistance = 0;
//				if (previousLocation == null || (traveledDistance = previousLocation.distanceTo(currentGPSLocation)) > update_travel_distance) {
					Log.i(TAG, "Traveled: " + traveledDistance + "m");
					mDatabaseService.createPlace(currentGPSLocation);
					updatePlaceList();
					previousLocation = currentGPSLocation;
//				} else {
//					Log.i(TAG, "Not traveled enough!");
//				}
			}
		} catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
			Log.e(TAG, "onTimerTick failed.", t);            
		}
	}
	
	/*
	 * Send localmessage that place has been created.
	 */
	private void updatePlaceList() {
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("placeCreated"));
	}	
}
