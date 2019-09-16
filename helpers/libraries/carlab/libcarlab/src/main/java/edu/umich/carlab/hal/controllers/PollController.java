package edu.umich.carlab.hal.controllers;

import android.content.Context;
import edu.umich.carlab.DataMarshal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;


/**
 * This is a really general wrapper which turns poll-response data access patterns into
 * subscribe-listen patterns. It does this by running a blocking (synchronous) Callable() inside
 * a thread which repeats in a fixed period. As the Callable() responds, this will broadcast the
 * data.
 *
 * TODO:
 * - Change the "EmitState" design to new broadcasts.
 */
public class PollController {
    Context ctx;
    DataMarshal dm;
    Set<String> keepRunning;
    final String TAG = "PollController";

    public PollController (Context ctx, DataMarshal dm) {
        this.dm = dm; this.ctx = ctx;
        keepRunning = new HashSet<>();
    }

    public void start (final String key, final Callable<Float> callable, final String DeviceName, final String SensorName, final int period) {
        keepRunning.add(key);
        // Starts a new thread to run this key
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (keepRunning.contains(key)) {
                    // Do the callable
                    try {
                        Float result = callable.call();
                        dm.broadcastData(DeviceName, SensorName, result, DataMarshal.MessageType.DATA);
                    } catch (Exception e) {
                        // Couldnt make the call.
                        // XXX How do we signify the error?
                        String errorMsg = "Sensor Error: " + e.getMessage();
                        dm.broadcastData(DeviceName, SensorName, -1f, DataMarshal.MessageType.ERROR);
                        //Log.e(TAG, errorMsg);

                        // Even if there's error, we should keep polling?
                        //keepRunning.remove(key);
                        //break;
                    }

                    try {
                        Thread.sleep(period);
                    } catch (Exception e) {}
                }
            }
        });

        thread.setName(key);
        thread.start();
    }

    public void stop (String key) {
        keepRunning.remove(key);
    }
}
