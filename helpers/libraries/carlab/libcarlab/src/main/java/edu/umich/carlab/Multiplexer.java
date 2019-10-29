package edu.umich.carlab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.carlab.loadable.App;
import edu.umich.carlab.utils.NotificationsHelper;

import static edu.umich.carlab.Constants.DUMP_BYTES;
import static edu.umich.carlab.Constants.DUMP_COLLECTED_STATUS;
import static edu.umich.carlab.Constants.Dump_Data_Mode_Key;

public class Multiplexer {
    final static String TAG = "Multiplexer";
    Map<String, Set<String>> dataMultiplexing;
    Map<String, Map<String, Long>> lastDataUpdate = new HashMap<>();
    SharedPreferences prefs;

    public void create() {
        dataMultiplexing = new HashMap<>();
    }

    String toMultiplexKey(String dev, String sen) {
        return dev + ":" + sen;
    }

    public void registerSensors (Map<String, App> runningApps) {
        String device, sensor, multiplexKey;

        for (Map.Entry<String, App> appEntry : runningApps.entrySet()) {
            List<Pair<String, String>> sensors = appEntry.getValue().getSensors();
            for (Pair<String, String> devSensor : sensors) {
                device = devSensor.first;
                sensor = devSensor.second;
                // Add this to the sensor multiplexing
                multiplexKey = toMultiplexKey(device, sensor);
                if (!dataMultiplexing.containsKey(multiplexKey))
                    dataMultiplexing.put(multiplexKey, new HashSet<String>());
                dataMultiplexing.get(multiplexKey).add(appEntry.getKey());
                lastDataUpdate.get(appEntry.getKey()).put(multiplexKey, 0L);
            }
        }


        Log.v(TAG, "Finished startup sequence. We are multiplexing these keys: ");
        for (Map.Entry<String, Set<String>> appEntry : dataMultiplexing.entrySet()) {
            Log.v(TAG, "Key: " + appEntry.getKey());
            for (String appName : appEntry.getValue()) {
                Log.v(TAG, "\t" + appName);
            }
        }
    }

    public void clear() {
        if (dataMultiplexing != null)
            dataMultiplexing.clear();
        dataMultiplexing = null;
    }


    public void addNewData (String information, Object value) {
        // route it!
        final Boolean dumpMode = prefs.getBoolean(Dump_Data_Mode_Key, false);
        long currTime = System.currentTimeMillis();

        if (dumpMode) {
            dataDumpStorage.add(dataObject);
            if (currTime > dataDumpLastBroadcastTime + dataDumpBroadcastEvery) {
                Intent intent = new Intent(DUMP_COLLECTED_STATUS);
                intent.putExtra(DUMP_BYTES, dataDumpStorage.size());
                sendBroadcast(intent);
                dataDumpLastBroadcastTime = currTime;
            }
            return;
        }

        if (dataObject == null) return;
        if (dataMultiplexing == null) return;
        if (prefs.getInt(
                Constants.Session_State_Key,
                TriggerSession.SessionState.PAUSED.getValue()
        ) == TriggerSession.SessionState.PAUSED.getValue())
            return;

        String multiplexKey = dataObject.device + ":" + dataObject.sensor;
        Intent statusIntent;

        if (dataMultiplexing != null && dataMultiplexing.containsKey(multiplexKey)) {
            dataObject.uid = uid;
            dataObject.tripid = tripid;

            Set<String> classNames = dataMultiplexing.get(multiplexKey);
            for (String appClassName : classNames) {
                App app = runningApps.get(appClassName);
                if (app == null) continue;

                // Throttle the data rate for each sensor
                if (currTime > lastDataUpdate.get(appClassName).get(multiplexKey) + DATA_UPDATE_INTERVAL_IN_MS) {
                    // The app gets new data
                    dataObject.appClassName = appClassName;
                    app.newData(dataObject);

                    if (!liveMode)
                        clTripWriter.addNewData(appClassName, dataObject);
                    lastDataUpdate.get(appClassName).put(multiplexKey, currTime);
                }

                // Update the notification
                // If the BT service scans in the meantime, it resets the notification to it's own thing
                if (currTime > lastNotificationUpdate + UPDATE_NOTIFICATION_INTERVAL) {
                    NotificationsHelper.setNotificationForeground(this, NotificationsHelper.Notifications.COLLECTING_DATA);
                    lastNotificationUpdate = currTime;
                }

                // Only broadcast state if it's changed since last time
                if ((lastStateUpdate.get(appClassName) != dataObject.dataType)) {
                    statusIntent = new Intent();
                    statusIntent.setAction(Constants.INTENT_APP_STATE_UPDATE);
                    statusIntent.putExtra("appClassName", appClassName);
                    statusIntent.putExtra("appState", dataObject.dataType);
                    CLService.this.sendBroadcast(statusIntent);
                    lastStateUpdate.put(appClassName, dataObject.dataType);
                }
            }
        } else {
            //Log.e(TAG, "CLService got data that no one asked for: " + dataObject.device + ", " + dataObject.sensor);
            // This happens often when the dependency sends out lots of data which no one cares about.
            // No need to flood LogCat with this.
        }

    }

    public void addMultiplexRoute (String information, String runningAlgorithmName) {
        // This will be routed from other languages to this input

        // Actually, even if it is internal I think we can use this. Basically when
    }

    public void addExternalMultiplexOutput (String information) {
        // This will be placed in the outbox from any algorithm that produces it
    }
}
