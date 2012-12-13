package fi.salminen.gpsmap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class PlaceListFrag extends ListFragment {
	private static final String TAG = "PlaceListFrag";
	
    OnPlaceSelectedListener mCallback;
    // The container Activity must implement this interface so the fragment can deliver messages
    public interface OnPlaceSelectedListener {
        /** Called by PlaceListFragment when a list item is selected */
        public void onPlaceSelected(String rowID);
    }
    
    public PlaceListFrag() {
	}
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("placeCreated"));
    }
    
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.i(TAG, "broadcast received: " + intent.getDataString() );
        	fillData();
        }
    };


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	// TODO Auto-generated method stub
    	super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
    	super.onStart();
    	
    	fillData();
    	
    	// When in two-panel layout, set the listview to highlight the selected list item
    	// (We do this during onStart because at the point the listview is available.)
    	if (getFragmentManager().findFragmentById(R.id.map) != null) {
    		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	}
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnPlaceSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPlaceSelectedListener");
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    /*
     * Fills listview with places from database.
     */
    @SuppressWarnings("deprecation")
	public void fillData() {
		// Get all of the places from the database and create the item list
    	PlacesDBAdapter mDbHelper = new PlacesDBAdapter(this.getActivity());
  		mDbHelper.openReadOnly();
    	Cursor c = mDbHelper.fetchAllPlaces();

		String[] from = new String[] { PlacesDBAdapter.KEY_ROWID, PlacesDBAdapter.KEY_NAME, PlacesDBAdapter.KEY_LATITUDE /*, PlacesDBAdapter.KEY_LONGITUDE */ };
		int[] to = new int[] { R.id.hiddenID, R.id.toptext, R.id.bottomtext };

		// Now create an array adapter and set it to display using our row
		SimpleCursorAdapter places = new SimpleCursorAdapter(this.getActivity(), R.layout.list_item, c, from, to);
		setListAdapter(places);
//		c.close();
		mDbHelper.close();
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		String rowID = ((TextView) view.findViewById(R.id.hiddenID)).getText().toString();
		
		// Notify the parent activity of selected item
        mCallback.onPlaceSelected(rowID);
        
        // Set the item as checked to be highlighted when in two-panel layout
        // TODO: Set item checked in places list don't work as expected.
        getListView().setItemChecked(position, true);
	}
	
	
}
