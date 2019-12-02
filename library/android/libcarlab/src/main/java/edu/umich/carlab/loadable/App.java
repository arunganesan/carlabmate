package edu.umich.carlab.loadable;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;
import edu.umich.carlab.hal.HardwareAbstractionLayer;

public abstract class App implements IApp {
    final String TAG = "appbase";
    final Integer SECONDS_PER_BUCKET = 5;
    final int PRINT_AVERAGE_EVERY = 100;
    public Activity parentActivity;
    public String name = null;
    public String middlewareName = null;
    public List<Pair<String, String>> sensors = new ArrayList<>();
    public List<String> dependencies = new ArrayList<>();
    public String description = "";
    public CLDataProvider cl;
    public boolean foregroundApp = false;
    public SharedPreferences prefs;
    protected Context context;
    // Historical bucketed data storage
    // Indexed by [device][sensor][N second bucket]
    protected boolean enableHistoricalLogging = false;
    boolean uploadData = true;
    // String URL = Constants.DEFAULT_UPLOAD_URL;

    Long startTime;
    // Latest DataObject storage
    Map<Registry.Information, DataMarshal.DataObject> latestData = new HashMap<>();
    Map<Registry.Information, Long> latestDataTime = new HashMap<>();
    Map<String, // Information
        Map<Long, // Seconds bucket
                List<DataSample>>> historicalData = new HashMap<>();

    public App() {
        this(null, null);
    }

    public App(CLDataProvider cl, Context context) {
        this.cl = cl;
        this.context = context;

        if (context != null)
            prefs = context.getSharedPreferences(this.getClass().getCanonicalName(), Context.MODE_PRIVATE);
    }


    /**
     * Load this value from the shared prefs. This is useful to keep track of values that must persist across
     * invocations of CarLab. For example, the previously saved fuel level.
     *
     * @param variableName
     * @param defaultValue
     * @return
     */
    protected double loadValue(String variableName, Double defaultValue) {
        return prefs.getFloat(variableName, defaultValue.floatValue());
    }

    protected void saveValue(String variableName, Double value) {
        prefs.edit().putFloat(variableName, value.floatValue()).commit();
    }

    public boolean isValidData(DataMarshal.DataObject dObject) {
        return (dObject.dataType == DataMarshal.MessageType.DATA)
                && (dObject.value != null);
    }

    protected void startClock() {
        startTime = SystemClock.uptimeMillis();
    }

    protected void endClock() {
        long endTime = SystemClock.uptimeMillis();
        Log.v(name, String.format("Ran in ms: %d", (endTime - startTime)));
    }

    /**
     * Returns the time bucket index
     * This function takes the timetamp, which is in milliseconds, and returns the nearest bucket that contains it
     * The bucket size is determined by the constant SECONDS_PER_BUCKET (default = 5)
     * I.e., all timestamps within a certain 5 second window are assigned the same bucket
     * <p>
     * Note, two very similar timestamps might still be divided across buckets.
     *
     * @param timestamp
     * @return
     */
    public Long bucketTime(Long timestamp) {
        return Math.round((timestamp.doubleValue()
                           / 1000.0
                           / SECONDS_PER_BUCKET));
    }

    /**
     * Gets the data that is closest to the seconds offset.
     * The sensor is the split raw sensor value. Consult the middleware or sensor sources for split sensor names
     * For example, if the data you need is phone/accel, you need to call this function with:
     * getDataAt(phone, accel_x, 0); getDataAt(phone, accel_y, 0); getDataAt(phone, accel_z, 0)
     * <p>
     * Note, this happens much more infrequently than adding to the historical data
     * We should put the heavy lifting of searching here and try to make insertion faster
     *
     * @param devsen
     * @param millisecondsOffset
     * @return
     */
    public DataSample getDataAt(String devsen, Long millisecondsOffset) {
        if (!enableHistoricalLogging) return null;
        /*
            Take current time
            Take seconds offset from current time
            Index into the rough ~5 second window (data indexed by 5 seconds)
            Search through 5 second window for the nearest timestamp
            Return that data
         */

        Long currTime = System.currentTimeMillis();
        Long searchTime = currTime - millisecondsOffset;
        Long bucket = bucketTime(searchTime);



        if (!historicalData.containsKey(devsen)) {
            Log.e(TAG, String.format("Sensor not found in historical data[%s]", devsen));
            return null;
        }

        if (!historicalData.get(devsen).containsKey(bucket)) {
            Log.e(TAG, String.format("Bucket not found in historical data[%s][%s][%d]", devsen, bucket));
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Long, List<DataSample>> keyValue : historicalData.get(devsen).entrySet()) {
                sb.append(String.format("%d [%d], ", keyValue.getKey(), keyValue.getValue().size()));
            }
            Log.v(TAG, String.format("Historical data only has: " + sb.toString()));
            return null;
        }


        List<DataSample> bucketSamples = historicalData.get(devsen)
                                                       .get(bucket);
        // Find the nearest datapoint
        // We can easily improve this by using a sorted list or binary searching
        Long nearestDiff = SECONDS_PER_BUCKET * 1000L; // At max this is the difference
        DataSample nearestSample = null;
        for (DataSample sample : bucketSamples) {
            Long diff = Math.abs(sample.time - searchTime);
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearestSample = sample;
            }
        }

        return nearestSample;
    }



    @CallSuper
    @Override
    public void newData(DataMarshal.DataObject dObject) {
        if (!isValidData(dObject)) return;
        Registry.Information information = dObject.information;
        latestData.put(information, dObject);
        latestDataTime.put(information, System.currentTimeMillis());

        String devsen = dObject.information.name;
        Long timestamp = dObject.time;

        // Only support historical logging for floats
        if (enableHistoricalLogging && dObject.information.dataType == Float.class) {
            Long bucket = bucketTime(timestamp);

            Float val;
            historicalData.putIfAbsent(devsen, new HashMap<Long, List<DataSample>>());
            historicalData.get(devsen).putIfAbsent(bucket, new ArrayList<DataSample>());
            historicalData.get(devsen)
                          .get(bucket)
                          .add(new DataSample(
                              timestamp,
                              (Float)dObject.value));
        }
    }

    /**
     * Gets the latest data for this sensor. There is no guarantee when this data was received or saved.
     * It returns the entire data object.
     */
    public DataMarshal.DataObject getLatestData(String information) {
        if (!latestData.containsKey(information)) return null;
        return latestData.get(information);
    }



    public void subscribe(String device, String sensor) {
        sensors.add(new Pair<>(device, sensor));
    }

    @Override
    public String getName() {
        if (name == null)
            throw new RuntimeException("The app must specify a name.");
        return name;
    }

    public String getMiddlewareName() {
        if (middlewareName == null)
            throw new RuntimeException("The app must specify a middleware name");
        return middlewareName;
    }

    @Override
    public List<Pair<String, String>> getSensors() {
        return sensors;
    }

    @Override
    public View initializeVisualization(Activity parentActivity) {
        this.parentActivity = parentActivity;
        return null;
    }

    @Override
    public void destroyVisualization() {
    }

    @Override
    public void shutdown() {
    }


    public void outputData(Registry.Information information, Object values) {
        if (values == null)
            return;

        DataMarshal.DataObject d = new DataMarshal.DataObject();
        d.time = System.currentTimeMillis();
        d.information = information;
        d.dataType = DataMarshal.MessageType.DATA;
        d.value = values;

        if (cl != null)
            cl.newData(d);
    }

    public DataMarshal.DataObject outputData(
            DataMarshal.DataObject dObject,
            Registry.Information info,
            Object value) {
        DataMarshal.DataObject secondaryDataObject = dObject.clone();
        secondaryDataObject.information = info;
        secondaryDataObject.value = value;
        cl.newData(secondaryDataObject);
        return secondaryDataObject;
    }

    /********************************
     * Activity callbacks that the MapView needs.
     * We mights'well make it part of the App standard so other's can benefit from it too
     *******************************/
    public void onCreate(Bundle bundle) {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public static class DataSample {
        public Long time;
        public Float errorInRequestedTime;
        public Float value;

        public DataSample(Long time, Float value) {
            this.time = time;
            this.value = value;
        }
    }
}
