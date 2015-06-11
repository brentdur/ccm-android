package com.brentondurkee.ccm.provider.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.auth.AuthRequests;
import com.brentondurkee.ccm.provider.SyncUtil;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by brenton on 6/9/15.
 */
public class RegIntentService extends IntentService{

    private final static String TAG="RegIntentService";


    private static final String[] TOPICS = {"global"};

    public static String PREF_GCM_TOKEN="gcm_token";

    public RegIntentService() {
        super(TAG);
        Log.v(TAG, "Construct");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "Handle Intent");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.v(TAG, "GCM Registration Token: " + token);
                if(!sharedPreferences.getString(PREF_GCM_TOKEN, "").equals(token)){
                    Log.v(TAG, "New Token");
                    sendRegistrationToServer(token);
                    sharedPreferences.edit().putString(PREF_GCM_TOKEN, token).commit();
                }


                // Subscribe to topic channels
                subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean("sentTokenToServer", true).apply();
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean("sentTokenToServer", false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent("registrationComplete");
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
//     * @param token The new token.
//     */
    private void sendRegistrationToServer(String gcm) throws Exception{
        // Add custom implementation, as needed.
        if(!AuthRequests.updateGCM(gcm, SyncUtil.getAuthToken())){
            throw new Exception("Update GCM Failed");
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
