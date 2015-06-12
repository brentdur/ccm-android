package com.brentondurkee.ccm.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by brenton on 6/10/15.
 */
public class AuthService extends Service {

    private final String TAG=getClass().getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Create Service");

    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Bind");
        Authenticator authenticator = new Authenticator((this));
        return authenticator.getIBinder();
    }
}
