package fi.salminen.gpsmap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class PlaceListFrag extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	@SuppressWarnings("unused")
	private static final String TAG = "PlaceListFrag";

    OnPlaceSelectedListener mCallback;

    private static final int PLACE_LIST_LOADER = 0x01;
    private SimpleCursorAdapter adapter;
    
    // The container Activity must implement this interface so the fragment can deliver messages
    public interface OnPlaceSelectedListener {
        /** Called by PlaceListFragment when a list item is selected */
        public void onPlaceSelected(String rowID);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] uiBindFrom = { PlaceListDB.KEY_ROWID, PlaceListDB.KEY_NAME, PlaceListDB.KEY_LATITUDE, PlaceListDB.KEY_LONGITUDE };
        int[] uiBindTo = { R.id.hiddenID, R.id.timeTextView, R.id.latitudeTextView, R.id.longitudeTextView };

        getLoaderManager().initLoader(PLACE_LIST_LOADER, null, this);
        adapter = new SimpleCursorAdapter(
        		getActivity().getApplicationContext(), R.layout.list_item, null,
                uiBindFrom, uiBindTo);
        setListAdapter(adapter);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	// TODO Auto-generated method stub
    	super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
    	super.onStart();
    	
    	// When in two-panel layout, set the listview to highlight the selected list item
    	// (We do this during onStart because at the point the listview is available.)
    	if (getFragmentManager().findFragmentById(R.id.map) != null) {
    		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	}
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { PlaceListDB.KEY_ROWID, PlaceListDB.KEY_NAME, PlaceListDB.KEY_LATITUDE, PlaceListDB.KEY_LONGITUDE };
	    CursorLoader cursorLoader = new CursorLoader(getActivity(),
	            PlaceListProvider.CONTENT_URI, projection, null, null, null);
	    return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.swapCursor(cursor);	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
