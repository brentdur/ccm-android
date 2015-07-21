/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.brentondurkee.ccm.Log;

/**
 * Created by brenton on 6/8/15.
 */
public class SyncService extends Service {
    private static SyncAdapter sAdapter = null;
    private static final Object sLock = new Object();

    @Override
    public void onCreate(){
        Log.v("Sync Service", "Service Created");
        synchronized (sLock){
            if(sAdapter == null){
                sAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    public IBinder onBind(Intent intent){
        return sAdapter.getSyncAdapterBinder();
    }
}
