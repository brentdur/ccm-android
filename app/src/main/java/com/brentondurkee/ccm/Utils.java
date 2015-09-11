package com.brentondurkee.ccm;

import android.app.Activity;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by brenton on 7/6/15.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static final String DOMAIN = "http://ccmtest.brentondurkee.com";

    public static String dateTo(String date){
        long[] parts = getParts(date);
        date = String.format("%2d days %02d:%02d:%02d", parts[4], parts[3], parts[2], parts[1]);
        return date;
    }

    public static String dateForm(String date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getMillis(date));
        return String.format("%2d/%02d @ %02d:%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public static long[] millisToDif(long millis){
        long[] ret = new long[4];
        //seconds
        ret[0] = (millis/1000)%60;
        //minutes
        ret[1] = (millis/60000)%60;
        //hours
        ret[2] = (millis/3600000)%24;
        //days
        ret[3] = (millis/86400000);

        return ret;
    }

    public static long getMillis(String date){
        Date time;
        try {
            date = date.replace("Z", " GMT");
            time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS zzz", Locale.US).parse(date);
            return time.getTime();
        }
        catch (ParseException e){
            Log.w("Time Parse Exception", e.toString());
        }
        return 0;
    }

    public static long[] getParts(String date){
        long[] ret = new long[5];
        ret[0] = getMillis(date) - System.currentTimeMillis();
        //seconds
        ret[1] = (ret[0]/1000)%60;
        //minutes
        ret[2] = (ret[0]/60000)%60;
        //hours
        ret[3] = (ret[0]/3600000)%24;
        //days
        ret[4] = (ret[0]/86400000);
        return ret;
    }

    public static Thread timer(String date, final TextView view, final Activity activity){
        final long[] pieces = getParts(date);
        return new Thread() {
            long secs = pieces[1];
            long mins = pieces[2];
            long hrs = pieces[3];
            long dys = pieces[4];
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                secs--;
                                if (secs < 0) {
                                    secs = 59;
                                    mins--;
                                    if (mins < 0) {
                                        mins = 59;
                                        hrs--;
                                        if (hrs < 0) {
                                            hrs = 23;
                                            dys--;
                                        }
                                    }
                                }

                                String date;
                                if (dys > 0) {
                                    date = String.format("%2d days %02d:%02d:%02d", dys, hrs, mins, secs);
                                } else {
                                    date = String.format("%02d:%02d:%02d", hrs, mins, secs);
                                }
                                view.setText(date);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    com.brentondurkee.ccm.Log.d(TAG, "Failure: " + e.toString());
                }
            }
        };
    }
}
