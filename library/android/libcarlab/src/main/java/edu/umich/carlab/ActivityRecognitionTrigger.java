package edu.umich.carlab;

import com.google.android.gms.location.DetectedActivity;

import edu.umich.carlab.utils.Utilities;

import static edu.umich.carlab.Constants.Last_Activity_Update;
import static edu.umich.carlab.Constants.Last_Time_In_Vehicle;


/**
 * Uses ActivityRecognitionClient. If the user is in a vehicle, we start. If the leave the vehicle,
 * we stop.
 */

public class ActivityRecognitionTrigger extends TriggerSession {
    final long IN_VEHICLE_TIMEOUT = 2*60*1000;


    @Override
    protected boolean checkSleepCondition() {
        int activityType = prefs.getInt(Last_Activity_Update, DetectedActivity.STILL);
        long lastTimeInVehicle = prefs.getLong(Last_Time_In_Vehicle, 0L);

        // Go to sleep IF we've been OUT OF VEHICLE for at least 1 minute
        return (
            (activityType != DetectedActivity.IN_VEHICLE) &&
            (System.currentTimeMillis() - lastTimeInVehicle > IN_VEHICLE_TIMEOUT)
        );
    }

    @Override
    protected boolean checkWakeupCondition() {
        int activityType = prefs.getInt(Last_Activity_Update, DetectedActivity.STILL);
        return (activityType == DetectedActivity.IN_VEHICLE);
    }

    @Override
    protected void reschedule(long triggerInMillis) {
        Utilities.scheduleOnce(context, getClass(), triggerInMillis);
    }
}