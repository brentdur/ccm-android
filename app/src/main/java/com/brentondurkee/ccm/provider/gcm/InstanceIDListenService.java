/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by brenton on 6/9/15.
 */
public class InstanceIDListenService extends InstanceIDListenerService{

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegIntentService.class);
        startService(intent);
    }
}
