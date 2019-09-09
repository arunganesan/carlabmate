package edu.umich.carlab;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * A class to make sure we create the JSON string the proper way
 */

public class DataMarshal {
    public static String TAG = "DataMarshal";
    private CLDataProvider cl;

    public DataMarshal(CLDataProvider cl) {
        this.cl = cl;
    }

    /**
     * @param device Device name. E.g. phone, watch, obd, can, web
     * @param sensor Sensor name. E.g. accel, heart rate, engine rpm, steering, weather
     * @param value  A string that was already computed
     */
    public void broadcastData(long timestamp, String device, String sensor, Float[] value, MessageType dataType) {
        DataObject d = new DataObject();
        d.time = timestamp;
        d.device = device;
        d.sensor = sensor;
        d.value = value;
        d.dataType = dataType;
        cl.newData(d);
    }

    // Overloaded helper function
    public void broadcastData(String device, String sensor, Float value, MessageType dataType) {
        //Double seconds = (new Double(System.currentTimeMillis())) / 1e+3;
        long seconds = System.currentTimeMillis();
        broadcastData(seconds, device, sensor, new Float[]{value}, dataType);
    }

    public enum MessageType {
        ERROR, STATUS, DATA;
    }

    public static class DataObject implements Serializable {
        public long time;
        public String device;
        public String sensor;
        public MessageType dataType;
        public Float[] value;
        public String uid;
        public String tripid;
        public String URL;
        public String appClassName;

        public DataObject clone() {
            DataObject dobj = new DataObject();
            dobj.time = time;
            dobj.device = device;
            dobj.sensor = sensor;
            dobj.dataType = dataType;
            dobj.value = value;
            dobj.uid = uid;
            dobj.tripid = tripid;
            dobj.URL = URL;
            dobj.appClassName = appClassName;
            return dobj;
        }

        public String toJson() {
            JSONObject jsonObject = new JSONObject();
            if (value.length != 1) {
                Log.e(TAG, "Error. This needs to be split before writing.");
                return null;
            }
            try {
                jsonObject.put("time", time);
                jsonObject.put("device", device);
                jsonObject.put("sensor", sensor);
                jsonObject.put("type", dataType);
                jsonObject.put("value", value[0]);
                jsonObject.put("uid", uid);
                jsonObject.put("tripid", tripid);
                jsonObject.put("url", URL);
                jsonObject.put("app", appClassName);
            } catch (JSONException jse) {
                return null;
            }
            return jsonObject.toString();
        }
    }

}
