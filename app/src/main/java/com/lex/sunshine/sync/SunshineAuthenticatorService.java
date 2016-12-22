package com.lex.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Alex on 21/12/2016.
 */

public class SunshineAuthenticatorService extends Service {
    /*************
     * Variables *
     ************/
    // Instance field that stores the authenticator object
    private SunshineAuthenticator authenticator;

    @Override
    public void onCreate(){
        this.authenticator = new SunshineAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call return the authenticator's IBinder
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.authenticator.getIBinder();
    }
}
