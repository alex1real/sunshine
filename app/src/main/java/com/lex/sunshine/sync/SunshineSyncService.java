package com.lex.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Alex on 22/12/2016.
 */

public class SunshineSyncService extends Service {

    private static final Object syncAdapterLock = new Object();
    private static SunshineSyncAdapter sunshineSyncAdapter = null;

    private final String LOG_TAG = SunshineSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "SunshineSyncService.onCreate()");

        synchronized(syncAdapterLock){
            if(sunshineSyncAdapter == null){
                sunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sunshineSyncAdapter.getSyncAdapterBinder();
    }
}
