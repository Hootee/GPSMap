package fi.salminen.gpsmap;

import java.lang.ref.WeakReference;

import android.os.Binder;
import android.util.Log;

/**
 * A generic implementation of Binder to be used for local services
 * @author Geoff Bruckner  12th December 2009
 *
 * @param <S> The type of the service being bound
 */

public class LocalBinder<S> extends Binder {
    private String TAG = "LocalBinder";
    private  WeakReference<S> mService;
    
    
    public LocalBinder(S service){
		Log.i(TAG, "LocalBinder");
        mService = new WeakReference<S>(service);
    }

    
    public S getService() {
		Log.i(TAG, "getService");
        return mService.get();
    }
}

