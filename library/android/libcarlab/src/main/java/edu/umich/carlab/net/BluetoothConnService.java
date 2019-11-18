package edu.umich.carlab.net;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.umich.carlab.CLService;
import edu.umich.carlab.Constants;
import edu.umich.carlab.utils.NotificationsHelper;

import java.util.Timer;

/**
 * Bluetooth service. Performs two main functions.
 * 1. Check if the OBD device is within range
 * 2. Do the setup process with a connected OBD device with error checking
 * <p>
 * Before any app performs Bluetooth-related activities, they have to bind to
 * this service and ask if the BT device is connected. If it isn't connected, they
 * don't try to perform the action. If the action fails and the pipe breaks, they send
 * a broadast.
 * <p>
 * This service listens to the broadcast. When it receives the broadcast, it tries to
 * re-establish the connection. The main thing is we have to ensure the state of the
 * Bluetooth socket remains across multiple invocations of this Service. I.e., this
 * service cannot be destroyed altogether during the duration of a CL connection.
 * We can do this by using a thread and keeping it alive.
 * <p>
 * Useful tips: https://medium.com/@workingkills/10-things-didn-t-know-about-android-s-service-component-a2880b74b2b3
 * => Services come and go all the time.
 * => Put business logic elsewhere
 * => Service runs on main thread by default
 * => ***There can only be one instance of a Service at a time***
 */

public class BluetoothConnService extends Service {
    public final String TAG = BluetoothConnService.class.getName();
    public final long MINUTE = 60 * 1000;
    final IBinder mBinder = new BluetoothConnService.LocalBinder();
    final long FIVE_MINUTES = MINUTE * 5;
    public long lastSuccessfulConnection = 0;
    SharedPreferences prefs;
    OBDConnTimerTask obdConnTimerTask;
    boolean connected = false;
    BluetoothDevice dev = null;
    BluetoothSocket socket = null;
    Timer timer = null;
    BroadcastReceiver bluetoothFailed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connectionFailed();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        // Or an intent
        if (intent != null) {
            Log.v(TAG, "Starting with Intent: " + intent.getAction());
            if (intent.getAction() != null && intent.getAction().equals(Constants.TRIGGER_BT_SEARCH)) {
                if (!connected) {
                    Timer tmpTimer = new Timer();
                    tmpTimer.schedule(new OBDConnTimerTask(this, prefs), 0);
                }
            }
        }

        return Service.START_STICKY;
    }

    /**
     * We should confirm that this is only called if the service is actually starting.
     * If it's already running and somebody binds to it, this shouldn't be called.
     */
    @Override
    public void onCreate() {
        // This should only happen when it is actually created.
        // That means, our previous state is gone
        Log.e(TAG, "OnCreate for BluetoothConn!!");
        IntentFilter btFailedFilter = new IntentFilter();
        btFailedFilter.addAction(Constants.BLUETOOTH_CONN_FAILED);
        registerReceiver(bluetoothFailed, btFailedFilter);

        // Create timer
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        obdConnTimerTask = new OBDConnTimerTask(this, prefs);
        timer = new Timer();
        timer.scheduleAtFixedRate(obdConnTimerTask, 0, MINUTE);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "On destroy bluetooth conn!!");
        unregisterReceiver(bluetoothFailed);
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }


    /****************************************************************
     * Public functions
     ***************************************************************/
    public boolean isActive() {
        return connected;
    }


    public BluetoothSocket getActiveSocket() {
        lastSuccessfulConnection = System.currentTimeMillis();
        return socket;
    }

    public synchronized void connectionFailed() {
        if (!connected) return;
        connected = false;
        socket = null;
        NotificationsHelper.setNotificationForeground(BluetoothConnService.this, NotificationsHelper.Notifications.DISCOVERY_FAIL);
        Timer tmpTimer = new Timer();
        tmpTimer.schedule(new OBDConnTimerTask(this, prefs), 0);
    }

    /**
     * This means the OBD is not nearby. That means we
     */
    public synchronized void unableToConnect() {
        // If we were just running data collection, then it's time to shut off
        if (lastSuccessfulConnection > System.currentTimeMillis() - FIVE_MINUTES) {
            // Set the off switch.
            prefs.edit().putBoolean(Constants.BT_FAILED, true).apply();
        }
    }

    public void connectionSucceeded(BluetoothSocket socket) {
        connected = true;
        // This means we haven't connected in a while.
        // And this re-establishment isn't due to a temporary break
        // And we just connected to the actual OBD device
        if (lastSuccessfulConnection < System.currentTimeMillis() - FIVE_MINUTES) {
            Intent intent = new Intent(this, CLService.class);
            intent.setAction(Constants.MASTER_SWITCH_ON);
            // startService(intent);
        }
        lastSuccessfulConnection = System.currentTimeMillis();
        this.socket = socket;
    }

    public class LocalBinder extends Binder {
        public BluetoothConnService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothConnService.this;
        }
    }
}


