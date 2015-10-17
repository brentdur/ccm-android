/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider.gcm;

import android.os.Bundle;

import com.brentondurkee.ccm.Log;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by brenton on 6/9/15.
 */
public class ListenService extends GcmListenerService{

    private final String TAG=getClass().getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("sync");
        Log.d(TAG, "Sync: " + message);

    }

}
