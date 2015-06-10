package com.brentondurkee.ccm.provider.gcm;

import android.content.Intent;

import com.brentondurkee.ccm.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

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
