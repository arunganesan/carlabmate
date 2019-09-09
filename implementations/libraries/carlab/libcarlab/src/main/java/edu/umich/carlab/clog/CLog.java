package edu.umich.carlab.clog;

import android.util.Log;

public class CLog {
    final static String VERBOSE = "V";
    final static String ERROR = "E";

    public static void v(String tag, String message) {
        CLogDatabaseHelper db = CLogDatabaseHelper.getInstance();
        if (db == null) return;
        db.save_log(tag, VERBOSE, message);
        Log.v(tag, message);
    }

    public static void e(String tag, String message) {
        CLogDatabaseHelper db = CLogDatabaseHelper.getInstance();
        if (db == null) return;
        db.save_log(tag, ERROR, message);
        Log.e(tag, message);
    }
}