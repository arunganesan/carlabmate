package edu.umich.carlab.hal;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.umich.carlab.CLService;
import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.hal.controllers.*;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.loadable.Middleware;
import edu.umich.carlab.sensors.ObdSensors;
import edu.umich.carlab.sensors.OpenXcSensors;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.sensors.WebSensors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


/**
 * Hardware abstraction layer acts as a proxy to different data sources. For example, it can read
 * from OBD-II sensors, the web, etc. It is initialized and invoked in @CarLabService. The main
 * paradigm is to subscribe and listen to new sources of data at a fixed period. Each time a data
 * source is requested, we will increment a counter in @ListenerMap. If this is the first time
 * the source is requested by anyone, we will create the necessary objects to access this data
 * (e.g. set up sensor listener or add a new OBD command to the queue). If someone unsubscribes
 * from the data, we will simply reduce the counter by 1. IF everyone has unsubscribed, then we will
 * call the appropriate destroy() commands.
 *
 * The reason behind the @ListenerMap design is that multiple "apps" in CarLab might subscribe to
 * the same data source. We don't want to create multiple listeners or multiple web-requests for
 * the same data.
 *
 * The reason behind the "polling" architecture (as opposed to the "probe and wait for response")
 * is that each sensor source has a slightly different access mechanism and this can help unify that.
 * For example, OBD-II sensors and web-requests have a poll-response mechanism whereas Android
 * sensors have a "subscribe-listen" mechanism. We can easily wrap the poll-response mechanisms by
 * creating a thread which polls periodically.
 *
 */

public class HardwareAbstractionLayer {
    private ListenerMap listenerMap;

    private WebController webController;
    private ObdController obdController;
    private PollController pollController;
    private PhoneController phoneController;
    private OpenXcController openXcController;

    private SharedPreferences prefs;


    private DataMarshal dm;
    private final static String TAG = "HAL";


    // This reflects if the data collection is currently running or not
    // It helps us toggle this using the UI or when the drive goes to the car or not
    // Eventually, this will evolve as we change what it means to "run data collection".
    // With multiple "apps" running on CarLab, each one will collect data in a separate way
    boolean running = false;

    // Setters and getters. Eventually this will be automatic.
    public boolean getRunning () { return running; }
    public void setRunning(boolean running) { this.running = running; }


    public HardwareAbstractionLayer(CLService cl) {
        Log.e(TAG, "Creating HAL.");

        this.dm = new DataMarshal(cl);
        prefs = PreferenceManager.getDefaultSharedPreferences(cl);

        listenerMap = new ListenerMap();

        webController = new WebController(cl, dm);
        obdController = new ObdController(cl, dm);
        pollController = new PollController(cl, dm);
        phoneController = new PhoneController(cl, dm);
        openXcController = new OpenXcController(cl, dm);
    }


    /**
     * All sources of data are polled periodically until they are turned off.
     * It is "on" if at least 1 person subscribes to it
     * If everyone unsubscribes, we stop getting that data
     * If multiple people subscribe, we still only fetch the data once (per period)
     *
     * Web data tends to poll ~once a minute
     * Sensor data polls at ~100 Hz
     * OBD data polls at frequency set in preferences (default: 1 Hz)
     *
     * @param device Name of the device. E.g. "phone", "obd", "web", "can"
     * @param sensor Name of the sensor within the device. E.g. "accelerometer", "engine rpm", "weather", "steering angle"
     * @return True or false whether the sensor was turned on (or is already turned on)
     */
    public boolean turnOnSensor (String device, String sensor) {
        String listenerKey = device + ":" + sensor;
        device = device.toLowerCase();

        boolean initializeSensor = listenerMap.addSubscriber(listenerKey);

        // If already initialized, return true
        if (!initializeSensor)
            return true;

        // Else, we will start this sensor.
        dm.broadcastData(device, sensor, Constants.GENERAL_STATUS, DataMarshal.MessageType.STATUS);

        switch (device) {
            case PhoneSensors.DEVICE:
                // Check if the service is already on and emitting data.
                try {
                    phoneController.startSensor(sensor);
                    Log.v(TAG, "Started sensor!: " + listenerKey);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error starting listener " + listenerKey + " got error: " + e.getMessage());
                    dm.broadcastData(device, sensor, Constants.GENERAL_ERROR, DataMarshal.MessageType.ERROR);

                    // No need to destroy this sensor since we didn't really make it
                    listenerMap.removeSubscriber(listenerKey);
                    return false;
                }
            case OpenXcSensors.DEVICE:
                if (!OpenXcSensors.validSensor(sensor)) {
                    dm.broadcastData(device, sensor,Constants.GENERAL_ERROR, DataMarshal.MessageType.ERROR);
                    listenerMap.removeSubscriber(listenerKey);
                    return false;
                }

                Callable<Float> oxcTask = openXcController.createTask(sensor);
                pollController.start(listenerKey, oxcTask, device, sensor, Constants.OXC_PERIOD);
                return true;
            case WebSensors.DEVICE:
                // We poll a simple call to the server at fixed intervals.
                // The "call to the server" happens in a separate thread.
                // Or it could be an AsyncTask or some higher-level wrapper to a thread
                // It is similar to adding the sensor to the sensor controller
                if (!webController.validSensor(sensor)) {
                    dm.broadcastData(device, sensor, Constants.GENERAL_ERROR, DataMarshal.MessageType.ERROR);
                    listenerMap.removeSubscriber(listenerKey);
                    return false;
                }

                Callable<Float> webTask = webController.createTask(sensor);
                pollController.start(listenerKey, webTask, device, sensor, 1000);
                return true;
            case ObdSensors.DEVICE:
                // We package the OBD call into a simple Runnable command. It uses a
                // pre-existing BT connection to just poll and get a response for a single command.
                // Then wrap that inside a thread and fire it periodically.

                // Validate the sensor, make sure it's OK
                Log.v(TAG, "Starting OBD inside thread ID: " + Thread.currentThread().getId());
                if (!obdController.isInitialized())
                    obdController.startupSync();
                if (!obdController.validSensor(sensor)) {
                    dm.broadcastData(device, sensor, Constants.GENERAL_ERROR, DataMarshal.MessageType.ERROR);
                    listenerMap.removeSubscriber(listenerKey);
                    return false;
                } else {
                    // Else, we're good.
                    Callable<Float> obdTask = obdController.createTask(sensor);
                    int pollPeriod = (int) (prefs.getFloat("obd_update_period_preference", 0.1f) * 1000);
                    pollController.start(
                            listenerKey, obdTask,
                            device, sensor,
                            pollPeriod);
                    return true;
                }
            default:
                dm.broadcastData(device, sensor, Constants.GENERAL_ERROR, DataMarshal.MessageType.ERROR);
                listenerMap.removeSubscriber(listenerKey);
                return false;
        }
    }


    /**
     * Splits data objects for writing to a file.
     *
     * @param dataObject
     * @return List of split data objects
     */
    public static List<DataMarshal.DataObject> splitDataObjects(DataMarshal.DataObject dataObject) {
        List<DataMarshal.DataObject> splitObjects = new ArrayList<>();

        Map<String, Float> splitValues = splitValues(dataObject);
        if (splitValues == null)  // This means there was nothing to split
            splitObjects.add(dataObject);
        else
            for (Map.Entry<String, Float> sensorValue : splitValues.entrySet()) {
                DataMarshal.DataObject dObjectClone = dataObject.clone();
                dObjectClone.sensor = sensorValue.getKey();
                dObjectClone.value = new Float [] { sensorValue.getValue() };
                splitObjects.add(dObjectClone);
            }

        return splitObjects;
    }

    /**
     * Split data objects based on their device data source.
     * This only returns values. It loses the rest of the meta data stored in data object.
     *
     * @return A map of the split values.
     */
    public static Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        String dev = dataObject.device;
        if (dev.equals(PhoneSensors.DEVICE))
            return PhoneSensors.splitValues(dataObject);
        else if (AppLoader.getInstance().getMiddleware().containsKey(dev)) {
            Middleware middleware = AppLoader.getInstance().getMiddleware().get(dataObject.device);
            return middleware.splitValues(dataObject);
        }

        // Else nothing to split.
        return null;
    }


    /**
     * Turn off a device's sensor. If all subscribers are removed, it will actually stop the
     * sensor by invoking the appropriate call. Otherwise, it will just reduce the subscriber count
     * and keep the listener running.
     *
     * @param device Device name
     * @param sensor Sensor name with this device
     * @return Whether or not this succeeded
     */
    public boolean turnOffSensor (String device, String sensor) {
        Log.v(TAG, "Turning off " + device + ":" + sensor);
        String listenerKey = device.toLowerCase() + ":" + sensor;
        device = device.toLowerCase();

        boolean destroySensor = listenerMap.removeSubscriber(listenerKey);
        if (!destroySensor) return true;

        // Else, shut down this sensor
        switch (device) {
            case PhoneSensors.DEVICE:
                phoneController.stopSensor(sensor);
                return true;
            case WebSensors.DEVICE:
            case OpenXcSensors.DEVICE:
                pollController.stop(listenerKey);
                return true;
            case ObdSensors.DEVICE:
                pollController.stop(listenerKey);
                if (listenerMap.returnAllListeners().size() == 0) {
                    obdController.destroy();
                }
                return true;
            default:
                return false;
        }
    }


    /**
     * We have to make sure to call "turnOffSensor" for as many listeners are bound to them
     */
    public void turnOffAllSensors() {
        int count;
        String [] parts;
        String device, sensor;

        Set<String> listeners = listenerMap.returnAllListeners();
        List<String> listenersCopy = new ArrayList<>(listeners);
        for (String listener : listenersCopy) {
            count = listenerMap.howManyListeners(listener);
            parts = listener.split(":");
            device = parts[0];
            sensor = parts[1];

            while (count-- > 0)
                turnOffSensor(device, sensor);
        }
    }
}
