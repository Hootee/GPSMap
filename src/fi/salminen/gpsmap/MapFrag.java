package fi.salminen.gpsmap;

import android.os.Bundle;
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
	@SuppressWarnings("unused")
	private static final String TAG = "MapFrag";
	
	private LatLng locLatLng = null;
	private float zoom = 15;
	private String message = null;
	
	/**
	 * Note that this may be null if the Google Play services APK is not available.
	 */
	private GoogleMap mMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

        mMap = getMap();
		initMap();
		
		// Lisätään markkeri, jos location on asetettu.
        if(message != null) {
        	updateMapMarker();
        }
        
	}
	
	public boolean setLocation(Bundle args) {
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
		mMap.clear();
		MarkerOptions marker = new MarkerOptions().position(locLatLng).title(message);
		mMap.addMarker(marker);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, zoom));
	}
}
