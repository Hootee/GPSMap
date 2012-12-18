package fi.salminen.gpsmap;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class PlaceListProvider extends ContentProvider {

	private static final String TAG = "PlaceListProvider";
	
	private static final String AUTHORITY = "fi.salminen.gpsmap.PlaceListProvider";
	public static final int PLACES = 100;
	public static final int PLACE_ID = 110;
	private static final String PLACES_BASE_PATH = "places";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + PLACES_BASE_PATH);
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
	        + "/mt-place";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
	        + "/mt-place";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, PLACES_BASE_PATH, PLACES);
		sURIMatcher.addURI(AUTHORITY, PLACES_BASE_PATH + "/#", PLACE_ID);
	}
	
	private PlaceListDB mDB;
		
	@Override
	public boolean onCreate() {
		mDB = new PlaceListDB(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = mDB.getWritableDatabase();
		int rowsAffected = 0;
		switch (uriType) {
		case PLACES:
			rowsAffected = sqlDB.delete(PlaceListDB.DATABASE_TABLE,
					selection, selectionArgs);
			break;
		case PLACE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(PlaceListDB.DATABASE_TABLE,
						PlaceListDB.KEY_ROWID + "=" + id, null);
			} else {
				rowsAffected = sqlDB.delete(PlaceListDB.DATABASE_TABLE,
						selection + " and " + PlaceListDB.KEY_ROWID + "=" + id,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		if (uriType != PLACES) {
			throw new IllegalArgumentException("Invalid URI for insert");
		}
		SQLiteDatabase sqlDB = mDB.getWritableDatabase();
		try {
			long newID = sqlDB.insertOrThrow(PlaceListDB.DATABASE_TABLE, null,
					values);
			if (newID > 0) {
				Uri newUri = ContentUris.withAppendedId(uri, newID);
				getContext().getContentResolver().notifyChange(uri, null);
				return newUri;
			} else {
				throw new SQLException("Failed to insert row into " + uri);
			}
		} catch (SQLiteConstraintException e) {
			Log.i(TAG, "Ignoring constraint failure.");
		}
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(PlaceListDB.DATABASE_TABLE);
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PLACE_ID:
			queryBuilder.appendWhere(PlaceListDB.KEY_ROWID + "="
					+ uri.getLastPathSegment());
			break;
		case PLACES:
			// no filter
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
		Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(),
				projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
