package edu.umich.carlab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import edu.umich.carlab.utils.Utilities;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.TriggerSession.SessionState.*;

public abstract class TriggerSession extends BroadcastReceiver {
    protected Context context;
    Handler scheduleHandler;
    final String TAG = "Trigger";
    SessionState sessionState;

    protected SharedPreferences prefs;

    public enum SessionState {
        ON(0), OFF(1), PAUSED(2);
        private final int value;
        SessionState(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        if (scheduleHandler == null)  {
            this.context = context;
            scheduleHandler = new Handler();
            scheduleHandler.post(checkState);
        } else {
            scheduleHandler.post(checkState);
        }
    }


    Runnable checkState = new Runnable() {
        @Override
        public void run() {
            prefs = getDefaultSharedPreferences(context);
            sessionState = SessionState.values()[prefs.getInt(Constants.Session_State_Key, 1)];

            Log.v(TAG, "Checking state");
            if ((sessionState == ON || sessionState == PAUSED) && checkSleepCondition()) {
                Log.v(TAG, "Going to sleep");
                sessionState = OFF;
                prefs
                        .edit()
                        .putInt(Constants.Session_State_Key, sessionState.getValue())
                        .commit();

                broadcastStatusChange();
                CLService.turnOffCarLab(context);
                Toast.makeText(context, "Turning off CarLab", Toast.LENGTH_LONG).show();
            } else if (sessionState == OFF && checkWakeupCondition()) {
                Log.v(TAG, "Waking up");
                sessionState = ON;
                prefs
                        .edit()
                        .putInt(Constants.Session_State_Key, sessionState.getValue())
                        .commit();

                CLService.turnOnCarLab(context);
                Toast.makeText(context, "Turning on CarLab", Toast.LENGTH_LONG).show();
                broadcastStatusChange();
            }

            if (sessionState == OFF) {
                reschedule(Constants.wakeupCheckPeriod);
            } else {
                reschedule(Constants.sleepCheckPeriod);
            }

            if (sessionState == OFF && Utilities.isMyServiceRunning(context, CLService.class)) {
                CLService.turnOffCarLab(context);
            };
        }
    };

    abstract protected void reschedule(long triggerInMillis);

    abstract protected boolean checkSleepCondition();

    abstract protected boolean checkWakeupCondition();

    void broadcastStatusChange() {
        context.sendBroadcast(new Intent(Constants.STATUS_CHANGED));
    }
}
