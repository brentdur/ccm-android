/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm;

/**
 * Created by brenton on 7/20/15.
 */
public class Log {
    final static boolean debug = true;
    final static String TAGPREFIX = "CCM";

    public static void v(String tag, String text){
        if(debug) {
            android.util.Log.v(TAGPREFIX + tag, text);
        }
    }
    public static void d(String tag, String text){
        if(debug) {
            android.util.Log.d(TAGPREFIX + tag, text);
        }
    }
    public static void w(String tag, String text){
        if(debug) {
            android.util.Log.w(TAGPREFIX + tag, text);
        }
    }
    public static void i(String tag, String text){
        if(debug) {
            android.util.Log.i(TAGPREFIX + tag, text);
        }
    }
}
