/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.brentondurkee.ccm.Log;

/**
 * Created by brenton on 6/10/15.
 * Service to run auth system
 */
public class AuthService extends Service {

    private final String TAG="AuthService";


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
