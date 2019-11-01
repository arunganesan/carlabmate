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
    String URL = Constants.DEFAULT_UPLOAD_URL;

    Long startTime;
    // Latest DataObject storage
    Map<String, DataMarshal.DataObject> latestData = new HashMap<>();
    Map<String, Long> latestDataTime = new HashMap<>();
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

    @CallSuper
    @Override
    public void newData(DataMarshal.DataObject dObject) {
        if (!isValidData(dObject)) return;
        String information = dObject.information;
        latestData.put(information, dObject);
        latestDataTime.put(information, System.currentTimeMillis());
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


    public void outputData(String information, Serializable values) {
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
            String info,
            Serializable value) {
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
