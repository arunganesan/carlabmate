package edu.umich.carlab.loadable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import edu.umich.carlab.DataMarshal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public abstract class Middleware {
    public abstract String getName();

    public List<String> getParameters() {
        return new ArrayList<>();
    }

    public void setParameter(Context context, String key, Float value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String namespaceKey = String.format("%s:%s", getName(), key);
        prefs.edit().putFloat(namespaceKey, value).apply();
    }

    public void setParameter(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String namespaceKey = String.format("%s:%s", getName(), key);
        prefs.edit().putString(namespaceKey, value).apply();
    }

    public Float getParameterOrDefault(Context context, String key, Float defaultVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String namespaceKey = String.format("%s:%s", getName(), key);
        return prefs.getFloat(namespaceKey, defaultVal);
    }

    public String getParameterOrDefault(Context context, String key, String defaultVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String namespaceKey = String.format("%s:%s", getName(), key);
        return prefs.getString(namespaceKey, defaultVal);
    }

    public Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        Map<String, Float> splitMap = new HashMap<>();
        String device = dataObject.device;
        splitMap.put(dataObject.sensor, dataObject.value[0]);
        return splitMap;
    }
}
