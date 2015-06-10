package com.brentondurkee.ccm.provider;

import android.accounts.Account;
import android.app.Application;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by brenton on 6/8/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    ContentResolver mResolver;
    final String TAG = "Sync Adapter";
    final String[] EVENT_PROJECTION = new String[]{
            DataContract.Event.COLUMN_NAME_ENTRY_ID,
            DataContract.Event.COLUMN_NAME_VERSION,
            DataContract.Event._ID};
    final String[] TALK_PROJECTION = new String[]{
            DataContract.Talk.COLUMN_NAME_ENTRY_ID,
            DataContract.Talk.COLUMN_NAME_VERSION,
            DataContract.Talk._ID};
    final String[] MSG_PROJECTION = new String[]{
            DataContract.Msg.COLUMN_NAME_ENTRY_ID,
            DataContract.Msg.COLUMN_NAME_VERSION,
            DataContract.Msg._ID};
    String eventFeed = new String("http://ccm.brentondurkee.com/api/events");
    String talkFeed = new String("http://ccm.brentondurkee.com/api/talks");
    String msgFeed = new String("http://ccm.brentondurkee.com/api/messages");

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSync) {
        super(context, autoInitialize, allowParallelSync);

        mResolver = context.getContentResolver();
    }

    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
       sync(eventFeed, DataContract.Event.CONTENT_URI, EVENT_PROJECTION, "event");
       sync(talkFeed, DataContract.Talk.CONTENT_URI, TALK_PROJECTION, "talk");
        sync(msgFeed, DataContract.Msg.CONTENT_URI, MSG_PROJECTION, "msg");

    }

    public void sync(String feed, Uri content, String[] projection, String type){
        InputStream stream = null;
        Log.v(TAG, "started for " +type);
        try{
            URL eventURL = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) eventURL.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            Log.v(TAG, "connected: " + feed);
            try {

                int response = conn.getResponseCode();
                Log.v(TAG, "response: "+response);
                if (response == 200) {
                    stream = conn.getInputStream();
                    Log.v(TAG, "start update");
                    Scanner reader = new Scanner(stream);
                    StringBuilder string = new StringBuilder();
                    do {
                        string.append(reader.nextLine());
                    } while(reader.hasNextLine());
                    Log.d(TAG, "Raw Json: "+string.toString());
                    updateDatabase(string.toString(), content, projection, type);
                    reader.close();
                }
            }
            finally {
                if(stream != null) {
                    stream.close();
                    Log.v(TAG, "close stream");
                }
            }
        }
        catch(MalformedURLException e){
            Log.w(TAG, "Malformed URL");
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }
        catch(JSONException e){
            Log.w(TAG, "JSONException: "+ e.toString());
        }
        catch(RemoteException e){
            Log.w(TAG, "RemoteException: "+ e.toString());
        }
        catch(OperationApplicationException e){
            Log.w(TAG, "OpperationAppException: "+ e.toString());
        }
    }

    public void updateDatabase(String data, Uri content, String[] projection, String type) throws JSONException, RemoteException, OperationApplicationException{
        JSONArray json = new JSONArray(data);
        ContentResolver mResolver = getContext().getContentResolver();
        ArrayList<JSONObject> objects = new ArrayList<JSONObject>(json.length());
        int updates = 0;
        int deletes = 0;
        int creates = 0;

        for(int i = 0; i<json.length(); i++){
            objects.add(json.getJSONObject(i));
        }

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>(json.length());
        Cursor cursor = mResolver.query(content, projection, null, null, null);

        while(cursor.moveToNext()){
            boolean found = false;
            for(int i = 0; i<objects.size(); i++){
                if(cursor.getString(0).equals(objects.get(i).getString("_id"))){
                    found = true;
                    if(objects.get(i).getInt("version") > cursor.getInt(1)){
                        Uri existing = content.buildUpon().appendPath(Integer.toString(cursor.getInt(2))).build();
                        batch.add(update(existing, objects.get(i), type));
                        updates++;
                    }
                    objects.remove(i);
                }
            }
            if(!found){
                Uri existing = content.buildUpon().appendPath(Integer.toString(cursor.getInt(2))).build();
                batch.add(ContentProviderOperation.newDelete(existing).build());
                deletes++;
            }
        }
        cursor.close();

        for(int i = 0; i < objects.size(); i++){
            batch.add(add(content, objects.get(i), type));
            creates++;
        }
        mResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        mResolver.notifyChange(content, null, false);
        Log.i(TAG, "Creates " + creates);
        Log.i(TAG, "Deletes " + deletes);
        Log.i(TAG, "Updates " + updates);
    }

    public ContentProviderOperation update(Uri content, JSONObject object, String type) throws JSONException {
        if(type.equals("event")){
            return updateEvent(content,
                    object.getString("title"),
                    object.getString("location"),
                    object.getString("date"),
                    object.getString("description"),
                    object.getInt("version"));
        }
        if(type.equals("talk")){
            return updateTalk(content,
                    object.getString("author"),
                    object.getString("subject"),
                    object.getString("date"),
                    object.getString("reference"),
                    object.getJSONArray("outline"),
                    object.getInt("version"));
        }
        if(type.equals("msg")){
            return updateMsg(content,
                    object.getString("from"),
                    object.getString("to"),
                    object.getString("subject"),
                    object.getString("date"),
                    object.getString("message"),
                    object.getInt("version"));
        }
        return null;
    }

    public ContentProviderOperation add(Uri content, JSONObject object, String type) throws JSONException {
        if(type.equals("event")){
            return addEvent(content,
                    object.getString("title"),
                    object.getString("location"),
                    object.getString("date"),
                    object.getString("description"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("talk")){
            return addTalk(content,
                    object.getString("author"),
                    object.getString("subject"),
                    object.getString("date"),
                    object.getString("reference"),
                    object.getJSONArray("outline"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("msg")){
            return addMsg(content,
                    object.getString("from"),
                    object.getString("to"),
                    object.getString("subject"),
                    object.getString("date"),
                    object.getString("message"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        return null;
    }

    public ContentProviderOperation updateEvent(Uri existing, String title, String location, String date, String description, int version){
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Event.COLUMN_NAME_TITLE, title)
                .withValue(DataContract.Event.COLUMN_NAME_LOCATION, location)
                .withValue(DataContract.Event.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Event.COLUMN_NAME_DESCRIPTION, description)
                .withValue(DataContract.Event.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addEvent(Uri existing, String title, String location, String date, String description, String id, int version){
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Event.COLUMN_NAME_TITLE, title)
                .withValue(DataContract.Event.COLUMN_NAME_LOCATION, location)
                .withValue(DataContract.Event.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Event.COLUMN_NAME_DESCRIPTION, description)
                .withValue(DataContract.Event.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Event.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation updateMsg(Uri existing, String from, String to, String subject, String date, String message, int version){
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Msg.COLUMN_NAME_FROM, from)
                .withValue(DataContract.Msg.COLUMN_NAME_TO, to)
                .withValue(DataContract.Msg.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Msg.COLUMN_NAME_SUBJECT, subject)
                .withValue(DataContract.Msg.COLUMN_NAME_MESSAGE, message)
                .withValue(DataContract.Msg.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addMsg(Uri existing, String from, String to, String subject, String date, String message, String id, int version){
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Msg.COLUMN_NAME_FROM, from)
                .withValue(DataContract.Msg.COLUMN_NAME_TO, to)
                .withValue(DataContract.Msg.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Msg.COLUMN_NAME_SUBJECT, subject)
                .withValue(DataContract.Msg.COLUMN_NAME_MESSAGE, message)
                .withValue(DataContract.Msg.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Msg.COLUMN_NAME_VERSION, version)
                .build();
    }


    public ContentProviderOperation updateTalk(Uri existing, String author, String subject, String date, String reference, JSONArray outline, int version) throws JSONException{
        String stringOutline =outline.getString(0);
        for (int i = 1; i< outline.length(); i++){
            stringOutline += "\",,,\"";
            stringOutline += outline.getString(i);
        }
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Talk.COLUMN_NAME_AUTHOR, author)
                .withValue(DataContract.Talk.COLUMN_NAME_SUBJECT, subject)
                .withValue(DataContract.Talk.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Talk.COLUMN_NAME_REFERENCE, reference)
                .withValue(DataContract.Talk.COLUMN_NAME_OUTLINE, stringOutline)
                .withValue(DataContract.Talk.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addTalk(Uri existing, String author, String subject, String date, String reference, JSONArray outline, String id, int version) throws JSONException{
        String stringOutline =outline.getString(0);
        for (int i = 1; i< outline.length(); i++){
            stringOutline += "\",,,\"";
            stringOutline += outline.getString(i);
        }
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Talk.COLUMN_NAME_AUTHOR, author)
                .withValue(DataContract.Talk.COLUMN_NAME_SUBJECT, subject)
                .withValue(DataContract.Talk.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Talk.COLUMN_NAME_REFERENCE, reference)
                .withValue(DataContract.Talk.COLUMN_NAME_OUTLINE, stringOutline)
                .withValue(DataContract.Talk.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Talk.COLUMN_NAME_VERSION, version)
                .build();
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
    }

    @Override
    public void onSyncCanceled(Thread thread) {
        super.onSyncCanceled(thread);
    }
}
