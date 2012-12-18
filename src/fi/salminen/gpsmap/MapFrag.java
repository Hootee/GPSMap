package fi.salminen.gpsmap;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFrag extends SupportMapFragment {
	private static final String TAG = "MapFrag";
	
	private LatLng locLatLng = null;
	private float zoom = 15;
	private String message;
	
	/**
	 * Note that this may be null if the Google Play services APK is not available.
	 */
	private GoogleMap mMap;
//	final static String ARG_LAT = "latitude";
//	final static String ARG_LON = "longitude";
//	final static String ARG_ZOOM = "zoom";
//	final static String ARG_MSG = "message";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		Log.i(TAG, "onCreateView");

		// If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
//        if (savedInstanceState != null) {
//        	locLatLng = new LatLng(savedInstanceState.getDouble(MainActivity.LATITUDE), savedInstanceState.getDouble(MainActivity.LONGITUDE));
//        	message = savedInstanceState.getString(MainActivity.MESSAGE);
//        	zoom = savedInstanceState.getFloat(MainActivity.ZOOM);
//        }
		return view;
	}

//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		Log.i(TAG, "onSaveInstanceState");
//
//		if (locLatLng != null) {
//			outState.putFloat(ARG_ZOOM, zoom);
//			outState.putDouble(ARG_LAT, locLatLng.latitude);
//			outState.putDouble(ARG_LON, locLatLng.longitude);
//			outState.putString(ARG_MSG, message);
//		}
//	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");

        mMap = getMap();
		initMap();
		
        if(locLatLng != null) {
        	updateMapMarker();
        }
        
	}
	
	public boolean setLocation(Bundle args) {
		Log.i(TAG, "setLocation");
        if (args != null) {
        	locLatLng = new LatLng(args.getDouble(MainActivity.LATITUDE), args.getDouble(MainActivity.LONGITUDE));
        	zoom = args.getFloat(MainActivity.ZOOM);
        	message = args.getString(MainActivity.MESSAGE);
        	return true;
        }        		
        return false;
	}
	
	private void initMap(){
	    UiSettings settings = mMap.getUiSettings();
	    settings.setAllGesturesEnabled(true);
	    settings.setMyLocationButtonEnabled(false);
	}
	
	public void updateMapMarker() {
		Log.i(TAG, "update marker: " + message);
		mMap.clear();
		MarkerOptions marker = new MarkerOptions().position(locLatLng).title(message);
		mMap.addMarker(marker);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, zoom));
	}
}
