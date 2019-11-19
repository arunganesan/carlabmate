package edu.umich.carlab.hal.controllers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.renderscript.Float3;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.utils.DevSen;

/**
 * This class polls all available sensors on the Android phone and keeps track of their sampling
 * rates and backs.
 */

public class PhoneController {
    final int DOWNSAMPLE_TO_EVERY = 3;
    final String DeviceName = "phone";
    final int GPSID = 12345;
    final String TAG = "PhoneController";
    int downsampleCounter = 0;
    Map<Integer, HandlerThread> listeningSensorHandlingThreads;
    // These are indexed by the sensor type ID. NOT the calling sensor name.
    // For example, accel_x,and accel_y both belong to the ACCELEROMETER type ID
    Map<Integer, Looper> listeningSensorLoopers;
    // Keeps track of individual sensors
    // The keys are sensor names (always 1D)
    Set<String> listeningSensors;
    // Used to make sure we're not registering multiple to teh same listener
    // The keys are Sensor Types (could be > 1 dimensional)
    private ConcurrentHashMap<Integer, SensorListenerClass> allListeners;
    private Context ctx;
    private DataMarshal dm;
    /**
     * Simple GPS callback
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult (LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                double latitude = location.getLatitude(), longitude = location.getLongitude(),
                        speed = location.getSpeed();

                speed *= 2.23694; // Convert to miles per hour
                long milliseconds = System.currentTimeMillis();

                Log.v(TAG, String.format("Got location. [time,lat,lon] = [%d,%f, %f]", milliseconds,
                                         latitude, longitude));

                Float3 data = new Float3((float)latitude, (float)longitude, (float)speed);
                dm.broadcastData(milliseconds, Registry.DevSenToInformation(
                        new DevSen(PhoneSensors.DEVICE, PhoneSensors.GPS)), data,
                                 DataMarshal.MessageType.DATA);
            }
        }

        ;
    };
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SensorManager mSensorManager;

    public PhoneController (Context ctx, DataMarshal dmm) {
        this.dm = dmm;
        this.ctx = ctx;
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        listeningSensors = new HashSet<String>();
        allListeners = new ConcurrentHashMap<>();
        listeningSensorLoopers = new HashMap<>();
        listeningSensorHandlingThreads = new HashMap<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
    }

    /**
     * Starts this sensor and emits this key upon returning.
     * Each "sensor name" is to get a ONE-dimensional value.
     * So, AX would only get the x acceleration of the IMU sensor.
     * and LATITUDE would only get latitude of the GPS sensor.
     * Any useful app would probably request all three.
     *
     * @param sensorName
     * @throws SensorNotFound
     * @throws SensorError
     */
    public void startSensor (String sensorName) throws SensorNotFound, SensorError {
        if (!PhoneSensors.validSensor(sensorName)) throw new SensorNotFound();

        if (listeningSensors.contains(sensorName)) return;

        if (PhoneSensors.isBroadcastSensor(sensorName)) {
            // Check if it is a valid sensor
            int typeId = PhoneSensors.sensorNameToType(sensorName);
            List<Sensor> allSensorsOfType = mSensorManager.getSensorList(typeId);
            if (allSensorsOfType == null || allSensorsOfType.size() == 0) throw new SensorError();
            Sensor mSensor = allSensorsOfType.get(0);
            if (mSensor == null) throw new SensorError();


            // It's a valid sensor, so we can "listen" to it
            listeningSensors.add(sensorName);

            // If it's already registered, no need to re-register.
            if (allListeners.containsKey(typeId)) return;

            // Sensor successfully referenced.
            // Adding a listener for this sensor.
            SensorListenerClass listener = new SensorListenerClass();
            allListeners.put(typeId, listener);


            // Receive on a new handler thread.
            Log.v(TAG, "Starting new thread for sensor: " + mSensor.getName());
            HandlerThread handlerThread = new HandlerThread(mSensor.getName());
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            Handler handler = new Handler(looper);
            listeningSensorHandlingThreads.put(typeId, handlerThread);
            listeningSensorLoopers.put(typeId, looper);
            mSensorManager
                    .registerListener(listener, mSensor, SensorManager.SENSOR_DELAY_UI, handler);

        } else if (sensorName.equals(PhoneSensors.GPS)) {

            int permissionGranted = ContextCompat
                    .checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionGranted == PackageManager.PERMISSION_DENIED) throw new SensorError();
            listeningSensors.add(sensorName);


            // This means we haven't set up the callback yet
            if (mLocationRequest == null) {

                HandlerThread handlerThread = new HandlerThread("gps");
                handlerThread.start();
                Looper looper = handlerThread.getLooper();
                listeningSensorHandlingThreads.put(GPSID, handlerThread);
                listeningSensorLoopers.put(GPSID, looper);

                try {
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(Constants.GPS_INTERVAL);
                    mFusedLocationClient
                            .requestLocationUpdates(mLocationRequest, mLocationCallback, looper);
                } catch (SecurityException se) {
                    Log.e(TAG, "GPS Security Exception.");
                    dm.broadcastData(Registry.DevSenToInformation(
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.GPS)),
                                     Constants.NO_GPS_PERMISSION_ERROR,
                                     DataMarshal.MessageType.ERROR);
                    return;
                }

                dm.broadcastData(Registry.DevSenToInformation(
                        new DevSen(PhoneSensors.DEVICE, PhoneSensors.GPS)),
                                 Constants.GPS_STARTED_STATUS, DataMarshal.MessageType.STATUS);
            }
        }
    }


    /******************************************************************************
     * ****************************************************************************
     * SENSOR CALLBACK FUNCTIONS
     * ****************************************************************************
     * These receive the data first. Then they send it out
     * ****************************************************************************
     * ****************************************************************************/

    /**
     * Remove this sensor from {@link #listeningSensors}.
     * Get the type corresponding to this sensor. If all other sensors from this type
     * are also removed, then unregister.
     *
     * @param sensor
     */
    public void stopSensor (String sensor) {
        listeningSensors.remove(sensor);
        Log.e(TAG, "stopSensor(" + sensor + ")");
        if (PhoneSensors.isBroadcastSensor(sensor)) {
            int sensorType = PhoneSensors.sensorNameToType(sensor);


            // Else, all related sensors have also been removed
            mSensorManager.unregisterListener(allListeners.get(sensorType));
            Looper looper = listeningSensorLoopers.get(sensorType);
            HandlerThread handlerThread = listeningSensorHandlingThreads.get(sensorType);

            if (looper != null) looper.quitSafely();
            if (handlerThread != null) handlerThread.quitSafely();
            Log.e(TAG, "Unregistering sensor type: " + sensorType);
            allListeners.remove(sensorType);
        } else if (sensor.equals(PhoneSensors.GPS)) {

            // Else, we've already unsubscribed both
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            Looper sensorLooper = listeningSensorLoopers.get(GPSID);
            if (sensorLooper != null) sensorLooper.quitSafely();

            HandlerThread sensorThread = listeningSensorHandlingThreads.get(GPSID);
            if (sensorThread != null) sensorThread.quitSafely();
            mLocationRequest = null;
        }
    }

    /**
     * If the sensor couldn't start for whatever reason
     */
    public class SensorError extends Exception {}

    /**
     * Sensor listener started for some sensor type.
     * Once it receives data, get list of related sensors
     * which are also in {@link #listeningSensors}. Then for each one
     * create a new package and dm.broadcastData.
     */
    private class SensorListenerClass implements SensorEventListener {
        String TAG = "PhoneController";

        @Override
        public void onAccuracyChanged (Sensor sensor, int i) { }

        @Override
        public void onSensorChanged (SensorEvent sensorEvent) {
            // https://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
            // Timestamp now and subtract out the change in time between timestamp and nanotime of system start
            //long ms = System.currentTimeMillis() + (sensorEvent.timestamp - System.nanoTime())/ 1000000L;
            // The time calculation is often buggy. We'll just override it with the current time.
            // This is a very very minor difference so it's not worth the bug
            long ms = System.currentTimeMillis();

            String formatted = "";
            int sensorType = sensorEvent.sensor.getType();
            String sensorGroupName = PhoneSensors.typeToSensorName(sensorType);
            Float[] values = new Float[sensorEvent.values.length];
            for (int i = 0; i < sensorEvent.values.length; i++) {
                values[i] = sensorEvent.values[i];
            }

            dm.broadcastData(ms, Registry.DevSenToInformation(
                    new DevSen(PhoneSensors.DEVICE, sensorGroupName)), values,
                             DataMarshal.MessageType.DATA);


        }
    }

    /**
     * If the sensor name is not valid
     */
    public class SensorNotFound extends Exception {}

}
