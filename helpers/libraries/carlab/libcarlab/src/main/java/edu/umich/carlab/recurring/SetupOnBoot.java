package edu.umich.carlab.recurring;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import edu.umich.carlab.utils.Utilities;

import static edu.umich.carlab.Constants.Trip_Id_Offset;


public class SetupOnBoot extends BroadcastReceiver {
    private static final String TAG = "SetupOnBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Utilities.schedule(context, UploadFiles.class, AlarmManager.INTERVAL_HALF_HOUR);

        // Wake up the main activity on boot
        // That will schedule everything else
        // Utilities.wakeUpMainActivity(context);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(Trip_Id_Offset, -1) == -1) {
            Utilities.keepTryingInit(context);
        }
    }
}
