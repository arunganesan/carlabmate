package edu.umich.carlab.net;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import edu.umich.carlab.utils.NotificationsHelper;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.Last_Activity_Update;
import static edu.umich.carlab.Constants.Last_Time_In_Vehicle;


public class ActivityRecogService extends Service {
    final String TAG = "ActivityRec";
    Handler mHandler;
    SharedPreferences prefs;

    public ActivityRecogService () {
        mHandler = new Handler();
    }

    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

    /**
     * We should confirm that this is only called if the service is actually starting.
     * If it's already running and somebody binds to it, this shouldn't be called.
     */
    @Override
    public void onCreate () {
        NotificationsHelper
                .setNotificationForeground(this, NotificationsHelper.Notifications.DISCOVERY);
    }

    @Override
    public void onDestroy () {

    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        prefs = getDefaultSharedPreferences(this);
        String status = "";
        ActivityRecognitionResult activity = ActivityRecognitionResult.extractResult(intent);
        if (activity != null) {
            status = "Activity: " + activity.toString();

            Log.v(TAG, status);
            prefs.edit().putInt(Last_Activity_Update, activity.getMostProbableActivity().getType())
                 .apply();
            if (activity.getMostProbableActivity().getType() == DetectedActivity.IN_VEHICLE) {
                prefs.edit().putLong(Last_Time_In_Vehicle, System.currentTimeMillis()).apply();
            }
            NotificationsHelper
                    .setNotificationForeground(this, NotificationsHelper.Notifications.STARTING);
        } else {
            status = "No activity detected";
            Log.v(TAG, status);
            prefs.edit().putInt(Last_Activity_Update, -1);
            NotificationsHelper.setNotificationForeground(this,
                                                          NotificationsHelper.Notifications.DISCOVERY_ERROR);
        }


        return Service.START_STICKY;
    }

    @Override
    public boolean onUnbind (Intent intent) {
        return false;
    }
}


