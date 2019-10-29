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

    public void broadcastData(long timestamp, String information, Serializable value) {
        DataObject d = new DataObject();
        d.time = timestamp;
        d.information = information;
        d.value = value;
        cl.newData(d);
    }

    // Overloaded helper function
    public void broadcastData(String information, Serializable value) {
        long seconds = System.currentTimeMillis();
        broadcastData(seconds, information, value);
    }

    public static class DataObject {
        public long time;
        public String information;
        public Serializable value;

        public DataObject clone() {
            DataObject dobj = new DataObject();
            dobj.time = time;
            dobj.value = value;
            return dobj;
        }

        public String toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("time", time);
                jsonObject.put("information", information);
                jsonObject.put("value", value.toString());
            } catch (JSONException jse) {
                return null;
            }
            return jsonObject.toString();
        }
    }

}
