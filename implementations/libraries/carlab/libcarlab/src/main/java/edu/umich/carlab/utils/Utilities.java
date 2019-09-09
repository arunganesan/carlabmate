package edu.umich.carlab.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import edu.umich.carlab.Constants;
import edu.umich.carlab.clog.CLog;
import edu.umich.carlab.io.CLTripWriter;
import edu.umich.carlab.io.UploadValuesTask;
import edu.umich.carlab.net.GetLatestTrip;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static edu.umich.carlab.Constants.Main_Activity;

/**
 * Created by arunganesan on 8/29/17.
 */

public class Utilities {

    //http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void schedule(Context context, Class<?> cls, long interval) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, cls), 0);
        alarmManager.cancel(alarmIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime(),
                interval,
                alarmIntent);
    }

    public static Class<?> getMainActivity(Context context) {
        final String TAG = "getMainActivity";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String mainActivityClassName = prefs.getString(Main_Activity, null);
        if (mainActivityClassName == null) {
            CLog.e(TAG, "Main Activity string was null");
        } else {
            try {
                Class<?> mainActivityClass = Class.forName(mainActivityClassName);
                return mainActivityClass;
            } catch (Exception e) {
                CLog.e(TAG, "Couldn't load main activity class");
            }
        }

        return null;
    }

    public static void wakeUpMainActivity(Context context) {
        final String TAG = "Wake Up Utilities";
        Class<?> mainActivityClass = getMainActivity(context);
        if (mainActivityClass == null) {
            CLog.e(TAG, "Couldn't load main activity class");
        } else {
            Intent intent = new Intent(context, mainActivityClass);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        }
    }

    public static void scheduleOnce(Context context, Class<?> cls, long inMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, cls), 0);
        alarmManager.cancel(alarmIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + inMillis,
                alarmIntent);
    }

    public static void keepTryingInit(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(Constants.Trip_Id_Offset, -1) != -1)
            return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, GetLatestTrip.class),
                0);
        alarmManager.cancel(alarmIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                alarmIntent);
    }


    public static void cancelInit(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(Constants.Trip_Id_Offset, -1) == -1)
            return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, GetLatestTrip.class),
                0);
        alarmManager.cancel(alarmIntent);
    }

    /**
     * Helper function which checks if we're connected and have WiFi connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnectedAndWifi(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // If isConnected is false, activeNetwork is null
        if (!isConnected) return false;


        // Check if the active network is WiFi
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }


    /**
     * List all trips in order.
     *
     * @param context
     * @return
     */
    public static File[] listAllTripsInOrder(Context context) {
        File homedir = CLTripWriter.GetTripsDir(context);
        File[] files = homedir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains(".json");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                //return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                return f2.getName().compareTo(f1.getName());
            }
        });
        return files;
    }


    /**
     * Return the trip ID from file name
     *
     * @param file
     * @return
     */
    public static int filenameToTripId(File file, Context context) {
        String filename = file.getName();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String tripKey = filename + ":trip";
        int thisTripId = prefs.getInt(tripKey, -1);
        return thisTripId;
    }


    /**
     * Upload the trip file and any corresponding media and survey responses.
     *
     * @param file
     */
    public static void uploadFile(File file, Context context) {
        // We need to loop through all stored files and get file names
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int experimentId = prefs.getInt(Constants.Experiment_Id, -1);
        UploadValuesTask uploadFileTask = new UploadValuesTask(file, experimentId);
        uploadFileTask.execute();
    }
}
