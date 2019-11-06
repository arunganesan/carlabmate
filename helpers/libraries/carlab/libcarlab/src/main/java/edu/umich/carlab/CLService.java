package edu.umich.carlab;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.umich.carlab.hal.HardwareAbstractionLayer;
import edu.umich.carlab.io.DataDumpWriter;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.net.LinkServerGateway;
import edu.umich.carlab.utils.DevSen;
import edu.umich.carlab.utils.NotificationsHelper;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.CARLAB_STATUS;
import static edu.umich.carlab.Constants.Dump_Data_Mode_Key;
import static edu.umich.carlab.Constants.LIVE_MODE;
import static edu.umich.carlab.Constants._STATUS_MESSAGE;

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
    final long DATA_UPDATE_INTERVAL_IN_MS = 0; // 100;
    final long STATE_UPDATE_EVERY = 100L;
    final String TAG = "CarLab Service";
    final long UPDATE_NOTIFICATION_INTERVAL = 5000;
    final long dataDumpBroadcastEvery = 500L;
    final IBinder mBinder = new LocalBinder();
    public long startTimestamp;
    protected Map<Class<? extends Algorithm>, Set<Registry.Information>> algorithmInputWiring =
            new HashMap<>();
    protected Map<String, Set<String>> dataMultiplexing;
    protected Map<String, Map<String, Long>> lastDataUpdate = new HashMap<>();
    protected Map<String, Long> lastStateUpdate = new HashMap<>();
    protected Set<DevSen> rawSensorsToStart = new HashSet<>();
    protected Strategy strategy;

    protected Set<String> toServerMultiplexing = new HashSet<>();
    boolean currentlyStarting = false;
    long dataDumpLastBroadcastTime = 0L;
    List<DataMarshal.DataObject> dataDumpStorage;
    HardwareAbstractionLayer hal;
    long lastNotificationUpdate = 0;
    LinkServerGateway linkServerGateway;
    boolean linkServerGatewayBound = false;
    Boolean liveMode;
    SharedPreferences prefs;
    Map<String, App> runningApps;
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
        loadRequirements();

        for (Algorithm.Function function : strategy.loadedFunctions) {
            // Send all input to this algorithm
            if (!algorithmInputWiring.containsKey(function.belongsTo))
                algorithmInputWiring.put(function.belongsTo, new HashSet<Registry.Information>());
            algorithmInputWiring.get(function.belongsTo).addAll(function.inputInformation);
        }

        for (Registry.Information info : strategy.saveInformation)
            addExternalMultiplexOutput(info);

    }

    public static void turnOffCarLab (Context c) {

    }

    public static void turnOnCarLab (Context c) {

    }

    public void addExternalMultiplexOutput (Registry.Information information) {
        toServerMultiplexing.add(information.name);
    }

    public void addMultiplexRoute (String infoname, Class<? extends Algorithm> algclass) {
        // This will be routed from other languages to this input
        // Actually, even if it is internal I think we can use this.
        if (!dataMultiplexing.containsKey(infoname))
            dataMultiplexing.put(infoname, new HashSet<String>());
        dataMultiplexing.get(infoname).add(algclass.getName());
        lastDataUpdate.get(algclass.getName()).put(infoname, 0L);


    }

    /**
     * Brings all apps to life using their class name
     */
    private void bringAppsToLife () {
        for (Class<? extends Algorithm> algo : algorithmInputWiring.keySet()) {
            String classname = algo.getName();
            try {
                Constructor<?> constructor =
                        algo.getConstructor(CLDataProvider.class, Context.class);
                Algorithm appInstance = (Algorithm) constructor.newInstance(this, this);
                runningApps.put(classname, appInstance);
                lastDataUpdate.put(classname, new HashMap<String, Long>());
            } catch (Exception e) {
                Log.e(TAG, "Error creating alive app: " + e + algo.getCanonicalName());
            }
        }


        for (Class<? extends Algorithm> alg : algorithmInputWiring.keySet())
            for (Registry.Information info : algorithmInputWiring.get(alg)) {
                addMultiplexRoute(info.name, alg);
                if (info.lowLevelSensor != -1)
                    // Get the mapping from information to device/sensor that the hal speaks
                    rawSensorsToStart.add(info.devSensor);
            }

        for (DevSen devSen : rawSensorsToStart)
            hal.turnOnSensor(devSen.device, devSen.sensor);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    public List<Algorithm.Function> getLoadedFunctions () {
        return strategy.loadedFunctions;
    }

    public boolean isCarLabRunning () {
        return runningDataCollection;
    }

    protected void loadRequirements () {

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
        long currTime = System.currentTimeMillis();

        if (dataObject == null) return;
        if (dataMultiplexing == null) return;
        if (prefs.getInt(Constants.Session_State_Key,
                         TriggerSession.SessionState.PAUSED.getValue()) ==
            TriggerSession.SessionState.PAUSED.getValue()) return;

        // String multiplexKey = dataObject.device + ":" + dataObject.sensor;
        Intent statusIntent;
        String infoname = dataObject.information;

        if (toServerMultiplexing.contains(infoname)) linkServerGateway.addNewData(dataObject);


        // Only broadcast state if it's changed since last time
        if (!lastStateUpdate.containsKey(infoname)) lastStateUpdate.put(infoname, 0L);

        if (currTime > lastStateUpdate.get(infoname) + STATE_UPDATE_EVERY) {
            statusIntent = new Intent();
            statusIntent.setAction(Constants.INTENT_APP_STATE_UPDATE);
            statusIntent.putExtra("information", infoname);
            statusIntent.putExtra("value", dataObject.value);
            CLService.this.sendBroadcast(statusIntent);
            lastStateUpdate.put(infoname, currTime);
        }


        if (dataMultiplexing != null && dataMultiplexing.containsKey(infoname)) {
            Set<String> classNames = dataMultiplexing.get(infoname);
            for (String appClassName : classNames) {
                App app = runningApps.get(appClassName);
                if (app == null) continue;

                // Throttle the data rate for each sensor
                if (currTime >
                    lastDataUpdate.get(appClassName).get(infoname) + DATA_UPDATE_INTERVAL_IN_MS) {
                    app.newData(dataObject);
                    lastDataUpdate.get(appClassName).put(infoname, currTime);
                }

                // Update the notification
                // If the BT service scans in the meantime, it resets the notification to it's own thing
                if (currTime > lastNotificationUpdate + UPDATE_NOTIFICATION_INTERVAL) {
                    NotificationsHelper.setNotificationForeground(this,
                                                                  NotificationsHelper.Notifications.COLLECTING_DATA);
                    lastNotificationUpdate = currTime;
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

            Log.e(TAG, "Service on start cmd: " + startTimestamp);
            NotificationsHelper.setNotificationForeground(this,
                                                          NotificationsHelper.Notifications.COLLECTING_DATA);

            startupSequence();
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
        }

        dataMultiplexing = null;
        runningApps = null;

        if (dumpMode) {
            DataDumpWriter dumpWriter = new DataDumpWriter(this);
            dumpWriter.dumpData(dataDumpStorage);
            dataDumpStorage.clear();
            prefs.edit().putBoolean(Dump_Data_Mode_Key, false).commit();
        }

        Intent stoppedIntent = new Intent(CARLAB_STATUS);
        stoppedIntent.putExtra(_STATUS_MESSAGE, "Stopped");
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

        bindService(new Intent(this, LinkServerGateway.class), mConnection,
                    Context.BIND_AUTO_CREATE);

        final Boolean dumpMode = prefs.getBoolean(Dump_Data_Mode_Key, false);
        if (dumpMode) dataDumpStorage = new ArrayList<>();

        Runnable startupTask = new Runnable() {
            @Override
            public void run () {
                Log.e(TAG, "Starting CL! Thread ID: " + Thread.currentThread().getId());
                runningApps = new HashMap<>();
                dataMultiplexing = new HashMap<>();
                lastDataUpdate = new HashMap<>();
                hal = new HardwareAbstractionLayer(CLService.this);

                bringAppsToLife();
                linkServerGateway.scheduleUploads();


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
                doneIntent.setAction(CARLAB_STATUS);
                doneIntent.putExtra(_STATUS_MESSAGE, "Running");
                CLService.this.sendBroadcast(doneIntent);
            }
        };

        Thread startupThread = new Thread(startupTask);
        startupThread.setName("Startup Thread");
        startupThread.start();
    }


    public class LocalBinder extends Binder {
        public CLService getService () {
            // Return this instance of LocalService so clients can call public methods
            return CLService.this;
        }
    }
}
