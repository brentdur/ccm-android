package com.brentondurkee.ccm.auth;

import android.accounts.AccountManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by brenton on 6/10/15.
 */
public class AuthRequests {

    private final static String TAG = "AuthRequests";

    private final static String signUpUrl="http://ccm.brentondurkee.com/api/users";
    private final static String signInUrl="http://ccm.brentondurkee.com/auth/local";
    private final static String GCMUrl="http://ccm.brentondurkee.com/api/users/gcm";

    //TODO: fill all exceptions
    public static String userSignUp(final String name, final String email, final String pass, String authType){
        Log.v(TAG, "Start Signup");
        String token = "";
        try {
            URL url = new URL(signUpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setChunkedStreamingMode(0);

                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                PrintWriter output = new PrintWriter(out);
                String req = String.format("name=%s&email=%s&password=%s", name, email, pass);
                output.print(req);
                output.close();

                int response = conn.getResponseCode();
                Log.v(TAG, "Response: " + response);
                if (response == 200) {
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    Scanner input = new Scanner(is);
                    JSONObject data = new JSONObject(input.nextLine());
                    token = data.getString("token");
                    Log.v(TAG, "Token: " + token);

                }
            }
            catch(JSONException e){
                Log.w(TAG, "JSON Exception");
            }
            finally {
                conn.disconnect();
            }
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return token;
    }

    public static boolean updateGCM(final String gcm, final String token){
        Log.v(TAG, "Start GCM");
        boolean good = false;
        try {
            URL url = new URL(GCMUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("gcm=%s", gcm);
            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
             }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }


    public static String userSignIn(final String email, final String pass, String authType){
        Log.v(TAG, "Start Signin");
        String token = "";
        try {
            URL url = new URL(signInUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setChunkedStreamingMode(0);

                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                PrintWriter output = new PrintWriter(out);
                String req = String.format("email=%s&password=%s", email, pass);
                output.print(req);
                output.close();

                int response = conn.getResponseCode();
                Log.v(TAG, "Response: " + response);
                if (response == 200) {
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    Scanner input = new Scanner(is);
                    JSONObject data = new JSONObject(input.nextLine());
                    token = data.getString("token");
                    Log.v(TAG, "Token: " + token);
                }
            }
            catch(JSONException e){
                Log.w(TAG, "JSON Exception");
            }
            finally {
                conn.disconnect();
            }
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return token;
    }
}
