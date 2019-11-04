package edu.umich.carlab.packaged;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.TriggerSession;
import edu.umich.carlab.hal.HardwareAbstractionLayer;
import edu.umich.carlab.hal.TraceReplayer;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.io.DataDumpWriter;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.net.LinkServerGateway;
import edu.umich.carlab.utils.DevSen;
import edu.umich.carlab.utils.NotificationsHelper;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.CARLAB_NOTIFICATION_ID;
import static edu.umich.carlab.Constants.CLSERVICE_STOPPED;
import static edu.umich.carlab.Constants.DONE_INITIALIZING_CL;
import static edu.umich.carlab.Constants.DUMP_BYTES;
import static edu.umich.carlab.Constants.DUMP_COLLECTED_STATUS;
import static edu.umich.carlab.Constants.Dump_Data_Mode_Key;
import static edu.umich.carlab.Constants.LIVE_MODE;
import static edu.umich.carlab.Constants.Load_From_Trace_Key;
import static edu.umich.carlab.Constants.Trip_Id_Offset;
import static edu.umich.carlab.Constants.UID_key;

/**
 * CL Service has life beyond the presense of an activity.
 * CL Service is responsible for keeping Apps alive.
 * When CL starts for the first time, it will set everything up and by default turn on the master
 * switch. The master switch fires an intent with the appropriate action and calls CLService's
 * onStartCommand().
 * <p>
 * * When master switch is turned on,
 * Start CLS as foreground service (it sets notification, won't die even if app swipe closed)
 * CLS instantiates all Apps that are active and has public functions for turning on/off apps
 * It reads the list of active apps
 * For each app, it gets the list of required sensors
 * For each sensors of the app, it uses the Hal to register that sensor
 * And it registers a listener for that sensor
 * Then, when it receives data, it goes through all apps that need that data, and sends it to the app
 * * When master switch is turned off
 * Call CLS destructor (through binding)
 * Unset the notifications
 * Tell all apps that we’re going to shut them off
 * Call stopSelf()
 */

public class CLService extends Service implements CLDataProvider {
    static boolean runningDataCollection = false;
    final int CL_NOTIFICATION_ID = CARLAB_NOTIFICATION_ID;
    final long DATA_UPDATE_INTERVAL_IN_MS = 0; // 100;
    final String TAG = "CarLab Service";
    final long UPDATE_NOTIFICATION_INTERVAL = 5000;
    final long dataDumpBroadcastEvery = 500L;
    final IBinder mBinder = new LocalBinder();
    public long startTimestamp;
    boolean currentlyStarting = false;
    long dataDumpLastBroadcastTime = 0L;
    List<DataMarshal.DataObject> dataDumpStorage;
    Map<String, Set<String>> dataMultiplexing;
    Set<DevSen> rawSensorsToStart = new HashSet<>();
    HardwareAbstractionLayer hal;
    // Downclock everything to 50 ms at least.
    // Eventually we want to add the option for apps to specify the data rate.
    Map<String, Map<String, Long>> lastDataUpdate = new HashMap<>();
    long lastNotificationUpdate = 0;
    Map<String, DataMarshal.MessageType> lastStateUpdate = new HashMap<>();
    LinkServerGateway linkServerGateway;
    boolean linkServerGatewayBound = false;
    Boolean liveMode;
    SharedPreferences prefs;
    TraceReplayer replayer;
    Map<String, App> runningApps;
    Set<String> toServerMultiplexing;
    String uid, tripid;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LinkServerGateway.LocalBinder binder = (LinkServerGateway.LocalBinder) service;
            linkServerGateway = binder.getService();

            linkServerGatewayBound = true;
        }

        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            linkServerGateway = null;
            linkServerGatewayBound = false;
        }
    };



    public CLService () {
        Log.e(TAG, "Service constructor");
    }

    public static void turnOffCarLab (Context context) {
        Intent intent = new Intent(context, CLService.class);
        intent.setAction(Constants.MASTER_SWITCH_OFF);
        context.startService(intent);
    }

    public static void turnOnCarLab (Context context) {
        // This means we havent' connected in a while.
        // And this re-establishment isn't due to a temporary break
        // And we just connected to the actual OBD device

        Intent intent = new Intent(context, CLService.class);
        intent.setAction(Constants.MASTER_SWITCH_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void addExternalMultiplexOutput (String information) {
        // This will be placed in the outbox from any algorithm that produces it
        // add this to a special set.
        // IF we reach this data, then go to the bound "packet" guy and place it?
        toServerMultiplexing.add(information);
    }

    public void addMultiplexRoute (String information, Algorithm algorithm) {
        // This will be routed from other languages to this input
        // Actually, even if it is internal I think we can use this.
        if (!dataMultiplexing.containsKey(information))
            dataMultiplexing.put(information, new HashSet<String>());
        dataMultiplexing.get(information).add(algorithm.getName());
        lastDataUpdate.get(algorithm.getName()).put(information, 0L);

        // TODO Make sure wiring uses the right String to key into data multiplexing
        // Anyway, pretty sure this is new territory lol

        if (AlgorithmSpecs.RawSensors.contains(information)) {
            // Get the mapping from information to device/sensor that the hal speaks
            rawSensorsToStart.add(AlgorithmSpecs.DevSenMapping.get(information));
        }
    }

    /**
     * Brings all apps to life using their class name
     */
    private void bringAppsToLife () {

        List<App> apps = AppLoader.getInstance().instantiateApps(this, this);
        for (App appInstance : apps) {
            String classname = appInstance.getClass().getCanonicalName();
            runningApps.put(classname, appInstance);
            lastStateUpdate.put(classname, null);
            lastDataUpdate.put(classname, new HashMap<String, Long>());
        }

        for (DevSen devSen : rawSensorsToStart) {
            hal.turnOnSensor(devSen.device, devSen.sensor);
        }
    }

    public boolean carlabCurrentlyStarting () {
        return currentlyStarting;
    }

    public Map<String, App> getAllRunningApps () {
        return runningApps;
    }

    /**
     * The app fragment will bind to this on resume and get the initial values from this function
     *
     * @param classname
     * @return
     */
    public DataMarshal.MessageType getLastStateUpdate (String classname) {
        if (!lastStateUpdate.containsKey(classname)) return null;
        else return lastStateUpdate.get(classname);
    }

    /**
     * Outside people can bind to this service and get running apps.
     * This is used in app details. That binds to this
     * service and renders the visualization of the running app.
     *
     * @param classname
     * @return
     */
    public App getRunningApp (String classname) {
        if (runningApps == null) return null;
        if (!runningApps.containsKey(classname)) return null;
        return runningApps.get(classname);
    }

    public boolean isCarLabRunning () {
        return runningDataCollection;
    }

    /**
     * Receives data. Based on our multiplexing, we may
     * need to feed in the data to the individual apps.
     * Data uploading is handled by a separate service.
     * <p>
     * We also don't do any UI update here. This is purely
     * for multiplexing. There's also a data receiver inside
     * the DCTFragment that's responsible for flashing various
     * colors.
     */
    public synchronized void newData (DataMarshal.DataObject dataObject) {
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
        if (prefs.getInt(Constants.Session_State_Key,
                         TriggerSession.SessionState.PAUSED.getValue()) ==
            TriggerSession.SessionState.PAUSED.getValue()) return;

        // String multiplexKey = dataObject.device + ":" + dataObject.sensor;
        String multiplexKey = dataObject.information;
        Intent statusIntent;

        if (dataMultiplexing != null && dataMultiplexing.containsKey(multiplexKey)) {
            Set<String> classNames = dataMultiplexing.get(multiplexKey);
            for (String appClassName : classNames) {
                App app = runningApps.get(appClassName);
                if (app == null) continue;

                // Throttle the data rate for each sensor
                if (currTime > lastDataUpdate.get(appClassName).get(multiplexKey) +
                               DATA_UPDATE_INTERVAL_IN_MS) {
                    app.newData(dataObject);

                    // if (!liveMode)
                    //    clTripWriter.addNewData(appClassName, dataObject);
                    if (toServerMultiplexing.contains(dataObject.information))
                        linkServerGateway.addNewData(dataObject);

                    lastDataUpdate.get(appClassName).put(multiplexKey, currTime);
                }

                // Update the notification
                // If the BT service scans in the meantime, it resets the notification to it's own thing
                if (currTime > lastNotificationUpdate + UPDATE_NOTIFICATION_INTERVAL) {
                    NotificationsHelper.setNotificationForeground(this,
                                                                  NotificationsHelper.Notifications.COLLECTING_DATA);
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

    @Override
    public IBinder onBind (Intent intent) {
        Log.e(TAG, "Service bind: " + startTimestamp);
        return mBinder;
    }

    @Override
    public void onCreate () {
        startTimestamp = System.currentTimeMillis();
        prefs = getDefaultSharedPreferences(this);
        liveMode = prefs.getBoolean(LIVE_MODE, false);
        Log.e(TAG, "Service On create: " + startTimestamp);
    }

    @Override
    public void onDestroy () {
        // The service is no longer used and is being destroyed
        Log.e(TAG, "Service on destroy: " + startTimestamp);
    }

    @Override
    public void onRebind (Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        final Boolean dumpMode = prefs.getBoolean(Dump_Data_Mode_Key, false);
        liveMode = prefs.getBoolean(LIVE_MODE, false);
        if (intent.getAction().equals(Constants.MASTER_SWITCH_ON)) {
            // Check if we're already running. If we are, then unload the running app and then start this
            if (isCarLabRunning()) {
                shutdownSequence();
            }

            int tripOffset = prefs.getInt(Trip_Id_Offset, -1);

            if (!liveMode) {
                if (tripOffset == -1) {
                    Toast.makeText(this, "Couldn't start CarLab. Unable to get existing trip ID.",
                                   Toast.LENGTH_SHORT).show();
                    return Service.START_NOT_STICKY;
                }
            }

            Log.e(TAG, "Service on start cmd: " + startTimestamp);
            NotificationsHelper.setNotificationForeground(this,
                                                          NotificationsHelper.Notifications.COLLECTING_DATA);

            startupSequence();
            Toast.makeText(this, "CarLab starting data collection. T=" + tripid, Toast.LENGTH_SHORT)
                 .show();
            return Service.START_NOT_STICKY;
        } else if (intent.getAction().equals(Constants.MASTER_SWITCH_OFF)) {
            if (dumpMode) {
                if (dataDumpStorage != null) Toast.makeText(this, String.format(Locale.getDefault(),
                                                                                "Turning off data dump. We collected %d total data points",
                                                                                dataDumpStorage
                                                                                        .size()),
                                                            Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Turning off CarLab data collection.", Toast.LENGTH_SHORT)
                     .show();
            }
            shutdownSequence();
            // Stop this service
            stopForeground(true);
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind (Intent intent) {
        // All clients have unbound with unbindService()
        //return mAllowRebind;
        Log.e(TAG, "Service unbind: " + startTimestamp);
        return false;
    }

    /**
     * Just call the shut down sequence of all apps.
     */
    private void shutdownAllApps () {
        for (App app : runningApps.values())
            app.shutdown();
    }

    private void shutdownSequence () {
        if (!runningDataCollection) return;
        final Boolean dumpMode = prefs.getBoolean(Dump_Data_Mode_Key, false);

        runningDataCollection = false;
        Log.e(TAG, "Shutting down CL! Thread ID: " + Thread.currentThread().getId());
        // Turn off all sensors
        if (hal != null) hal.turnOffAllSensors();

        // Shut down all apps
        if (runningApps != null) {
            shutdownAllApps();
            runningApps.clear();
        }


        // Reset the data multiplexer
        if (dataMultiplexing != null) {
            dataMultiplexing.clear();
            toServerMultiplexing.clear();
        }

        dataMultiplexing = null;
        toServerMultiplexing = null;
        runningApps = null;

        if (dumpMode) {
            DataDumpWriter dumpWriter = new DataDumpWriter(this);
            dumpWriter.dumpData(dataDumpStorage);
            dataDumpStorage.clear();
            prefs.edit().putBoolean(Dump_Data_Mode_Key, false).commit();
        }

        Intent stoppedIntent = new Intent();
        stoppedIntent.setAction(CLSERVICE_STOPPED);
        sendBroadcast(stoppedIntent);


        if (linkServerGatewayBound) {
            unbindService(mConnection);
            linkServerGatewayBound = false;
        }

    }

    /**
     * It reads the list of active apps
     * For each app, it gets the list of required sensors
     * For each sensors of the app, it uses the Hal to register that sensor
     * And it registers a listener for that sensor
     * Then, when it receives data, it goes through all apps that need that data, and sends it to the app
     */
    private void startupSequence () {
        runningDataCollection = true;
        currentlyStarting = true;
        uid = prefs.getString(UID_key, null);
        if (uid == null && !liveMode) {
            Log.e(TAG, "Problem setting the UID in carlab start");
        }

        bindService(new Intent(this, LinkServerGateway.class), mConnection,
                    Context.BIND_AUTO_CREATE);

        final Boolean dumpMode = prefs.getBoolean(Dump_Data_Mode_Key, false);
        if (dumpMode) dataDumpStorage = new ArrayList<>();

        Runnable startupTask = new Runnable() {
            @Override
            public void run () {
                Log.e(TAG, "Starting CL! Thread ID: " + Thread.currentThread().getId());
                boolean loadingFromTraceFile = prefs.getString(Load_From_Trace_Key, null) != null;
                hal = null; // = new HardwareAbstractionLayer(CLService.this);


                runningApps = new HashMap<>();
                dataMultiplexing = new HashMap<>();
                toServerMultiplexing = new HashSet<>();

                bringAppsToLife();

                if (!loadingFromTraceFile) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
                Log.v(TAG, "Just returned from bringing apps to life");

                // TODO Make sure things are properly wired

                Log.v(TAG, "Finished startup sequence. We are multiplexing these keys: ");
                for (Map.Entry<String, Set<String>> appEntry : dataMultiplexing.entrySet()) {
                    Log.v(TAG, "Key: " + appEntry.getKey());
                    for (String appName : appEntry.getValue()) {
                        Log.v(TAG, "\t" + appName);
                    }
                }


                // We can enable master switch now
                Intent doneIntent = new Intent();
                currentlyStarting = false;
                doneIntent.setAction(DONE_INITIALIZING_CL);
                CLService.this.sendBroadcast(doneIntent);
            }
        };

        Thread startupThread = new Thread(startupTask);
        startupThread.setName("Startup Thread");
        startupThread.start();
    }

    public void turnOnSensor (String device, String sensor) {
        // Only a few of the sensors actually turn on the sensors
        try {
            Thread.sleep(250);
        } catch (Exception e) {
        }

        hal.turnOnSensor(device, sensor);
    }

    public class LocalBinder extends Binder {
        public CLService getService () {
            // Return this instance of LocalService so clients can call public methods
            return CLService.this;
        }
    }
}
