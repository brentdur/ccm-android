/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.auth.AuthUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by brenton on 7/18/15.
 */
public class SyncPosts {

    public final static String EVENT_TITLE = "EVENT_TITLE";
    public final static String EVENT_DATE= "EVENT_DATE";
    public final static String EVENT_LOCATION= "EVENT_LOCATION";
    public final static String EVENT_ADDRESS = "EVENT_ADDRESS";
    public final static String EVENT_DESCRIPTION = "EVENT_DESCRIPTION";

    public final static String TALK_AUTHOR = "TALK_AUTHOR";
    public final static String TALK_SUBJECT = "TALK_SUBJECT";
    public final static String TALK_DATE = "TALK_DATE";
    public final static String TALK_REFERENCE = "TALK_REFERENCE";
    public final static String TALK_OUTLINE = "TALK_OUTLINE";

    public final static String MSG_FROM = "MSG_FROM";
    public final static String MSG_TO = "MSG_TO";
    public final static String MSG_DATE = "MSG_DATE";
    public final static String MSG_SUBJECT = "MSG_SUBJECT";
    public final static String MSG_MESSAGE = "MSG_MESSAGE";

    private final static String TAG = "SyncPosts";

    private final static String addEventUrl="http://ccm.brentondurkee.com/api/events";
    private final static String addTalkUrl="http://ccm.brentondurkee.com/api/talks";
    private final static String addMsgUrl="http://ccm.brentondurkee.com/api/messages";

    public static boolean addEvent(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Event");
        boolean good = false;
        try {
            URL url = new URL(addEventUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"title\": \"%s\",\"location\": \"%s\",\"date\": \"%s\",\"description\": \"%s\"", data.getString(EVENT_TITLE), data.getString(EVENT_LOCATION), data.getString(EVENT_DATE), data.getString(EVENT_DESCRIPTION));
            if(!data.getString(EVENT_ADDRESS).isEmpty()){
                req += String.format(",\"address\":\"%s\"", data.getString(EVENT_ADDRESS));
            }
            req += "}";
            Log.v(TAG, req);
            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean addTalk(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Talk");
        boolean good = false;
        try {
            URL url = new URL(addTalkUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"author\": \"%s\",\"subject\": \"%s\",\"date\": \"%s\",\"reference\": \"%s\",\"outline\":[", data.getString(TALK_AUTHOR), data.getString(TALK_SUBJECT), data.getString(TALK_DATE), data.getString(TALK_REFERENCE));
            String[] outline = data.getStringArray(TALK_OUTLINE);
            for(int i = 0; i<outline.length; i++){
                req += String.format("\"%s\"" ,outline[i]);
                if(i+1 < outline.length){
                    req += ",";
                }
            }
            req += "]}";

            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean addMsg(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Message");
        boolean good = false;
        try {
            URL url = new URL(addMsgUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"from\": \"%s\",\"to\": \"%s\",\"subject\": \"%s\",\"message\":\"%s\"}", data.getString(MSG_FROM), data.getString(MSG_TO), data.getString(MSG_SUBJECT), data.getString(MSG_MESSAGE));

            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }


}
