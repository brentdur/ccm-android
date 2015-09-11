/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider.gcm;

import android.os.Bundle;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.provider.SyncUtil;
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
        if(message.contains("all")){
            SyncUtil.TriggerRefresh();
        }
        if(message.contains("events")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_EVENT);
        }
        if(message.contains("talks")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_TALK);
        }
        if(message.contains("messages")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_MSG);
        }
        if(message.contains("signups")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_SIGNUP);
        }
        if(message.contains("groups")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_GROUP);
        }
        if(message.contains("locations")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_LOCATION);
        }
        if(message.contains("topics")){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_TOPIC);
        }
    }

}
