package edu.umich.carlab.packaged;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import edu.umich.carlab.CLService;
import edu.umich.carlab.Constants;
import edu.umich.carlab.utils.Utilities;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.CARLAB_STATUS;
import static edu.umich.carlab.Constants._STATUS_MESSAGE;

public class PackageTriggerSession extends BroadcastReceiver {
    final String TAG = "Trigger";
    protected Context context;
    protected SharedPreferences prefs;
    Handler scheduleHandler;
    SessionState sessionState;
    Runnable checkState = new Runnable() {
        @Override
        public void run () {
            prefs = getDefaultSharedPreferences(context);
            sessionState = SessionState.values()[prefs.getInt(Constants.Session_State_Key, 1)];
            // sessionState = SessionState.OFF;

            Log.v(TAG, "Checking state");
            if ((sessionState == PackageTriggerSession.SessionState.ON ||
                 sessionState == PackageTriggerSession.SessionState.PAUSED) &&
                checkSleepCondition()) {
                Log.v(TAG, "Going to sleep");
                sessionState = PackageTriggerSession.SessionState.OFF;
                prefs.edit().putInt(Constants.Session_State_Key, sessionState.getValue()).commit();

                broadcastStatusChange();
                CLService.turnOffCarLab(context);
                Toast.makeText(context, "Turning off CarLab", Toast.LENGTH_LONG).show();
            } else if (sessionState == PackageTriggerSession.SessionState.OFF &&
                       checkWakeupCondition()) {
                Log.v(TAG, "Waking up");
                sessionState = PackageTriggerSession.SessionState.ON;
                prefs.edit().putInt(Constants.Session_State_Key, sessionState.getValue()).commit();

                PackageCLService.turnOnCarLab(context);
                Toast.makeText(context, "Turning on CarLab", Toast.LENGTH_LONG).show();
                broadcastStatusChange();
            }

            if (sessionState == PackageTriggerSession.SessionState.OFF) {
                reschedule(Constants.wakeupCheckPeriod);
            } else {
                reschedule(Constants.sleepCheckPeriod);
            }

            if (sessionState == PackageTriggerSession.SessionState.OFF &&
                Utilities.isMyServiceRunning(context, PackageCLService.class)) {
                PackageCLService.turnOffCarLab(context);
            }
            ;
        }
    };

    void broadcastStatusChange () {
        Intent i = new Intent(CARLAB_STATUS);
        i.putExtra(_STATUS_MESSAGE, "Trigger status flipped");
        context.sendBroadcast(i);
    }

    protected boolean checkSleepCondition () {
        return !prefs.getBoolean(Constants.ManualChoiceKey, false);
    }

    protected boolean checkWakeupCondition () {
        return prefs.getBoolean(Constants.ManualChoiceKey, false);
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        if (scheduleHandler == null) {
            this.context = context;
            scheduleHandler = new Handler();
            scheduleHandler.post(checkState);
        } else {
            scheduleHandler.post(checkState);
        }
    }

    protected void reschedule (long triggerInMillis) {
        Utilities.scheduleOnce(context, getClass(), triggerInMillis);
    }

    public enum SessionState {
        ON(0), OFF(1), PAUSED(2);
        private final int value;

        SessionState (int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }
    }
}
