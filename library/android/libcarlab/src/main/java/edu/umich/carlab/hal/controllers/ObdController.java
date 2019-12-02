package edu.umich.carlab.hal.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.hal.JobMaker;
import edu.umich.carlab.io.ObdCommandJob;
import edu.umich.carlab.net.BluetoothConnService;
import edu.umich.carlab.sensors.ObdSensors;
import edu.umich.carlab.utils.DevSen;

/**
 * OBD Controller. Initializes Bluetooth connection with the device.
 * It queues commands synchronously.
 * Implements the validSensor and createTask commands of a JobMaker.
 */

public class ObdController implements JobMaker {
    final String TAG = this.getClass().getName();
    BluetoothConnService bluetoothConnService;
    Context ctx;
    DataMarshal dm;
    boolean mBound = false;
    SharedPreferences prefs;
    private BluetoothDevice dev = null;
    private boolean initialized = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothConnService.LocalBinder binder = (BluetoothConnService.LocalBinder) service;
            bluetoothConnService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            mBound = false;
        }
    };
    private BluetoothSocket sock = null;

    public ObdController (Context ctx, DataMarshal dm) {
        this.dm = dm;
        this.ctx = ctx;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public Callable<Float> createTask (final String sensor) {
        return new Callable<Float>() {
            @Override
            public Float call () throws Exception {
                //Log.v(TAG, "Created task: " + sensor);
                Float response = runCommandSync(ObdSensors.nameToCommand(sensor));
                //Log.v(TAG, "Got response: " + response);
                return response;
            }
        };
    }

    /**
     * Called if no one is listening to sensors anymore.
     */
    public void destroy () {
        if (mBound) {
            ctx.unbindService(mConnection);
            mBound = false;
        }
    }

    public boolean isInitialized () { return initialized; }

    Float runCommandSync (ObdCommand obdCommand) throws Exception {
        if (!mBound) throw new Exception("Not bound to BT service yet.");
        if (!bluetoothConnService.isActive()) throw new Exception("Not connected to OBD yet");
        sock = bluetoothConnService.getActiveSocket();
        if (sock == null) throw new Exception("Socket is null. Not initialized yet");
        ObdCommandJob job = new ObdCommandJob(obdCommand);
        try {
            job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
        } catch (IOException e) {
            bluetoothConnService.connectionFailed();
            throw new Exception("Bluetooth IO failed. Requesting re-connection");
        }
        float responseVal = ObdSensors.responseToFloat(job.getCommand());
        return responseVal;
    }

    /**
     * Some times we want to startup synchronously.
     * This happens when we start the data collection in a separate thread.
     */
    public void startupSync () {
        // This happens syncrhonously
        // dm.broadcastData(
        //         Registry.DevSenToInformation(new DevSen(ObdSensors.DEVICE, ObdSensors.FUEL_LEVEL)),
        //         Constants.STARTING_STATUS, DataMarshal.MessageType.STATUS);
        Log.v(TAG, "Starting OBD in thread: " + Thread.currentThread().getId());

        // Bind to Bluetooth Conn Service
        Intent intent = new Intent(ctx, BluetoothConnService.class);
        ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public boolean validSensor (String sensor) {
        return ObdSensors.validSensor(sensor);
    }
}
