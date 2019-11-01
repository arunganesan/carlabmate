package edu.umich.carlab;

import android.os.Message;
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


    public void broadcastData(long timestamp, String information, Serializable value, MessageType dataType) {
        DataObject d = new DataObject();
        d.time = timestamp;
        d.information = information;
        d.value = value;
        d.dataType = dataType;
        cl.newData(d);
    }

    // Overloaded helper function
    public void broadcastData(String information, Serializable value, MessageType dataType) {
        //Double seconds = (new Double(System.currentTimeMillis())) / 1e+3;
        long seconds = System.currentTimeMillis();
        broadcastData(seconds, information, value, dataType);
    }

    public void broadcastData(String information, Serializable value) {
        //Double seconds = (new Double(System.currentTimeMillis())) / 1e+3;
        long seconds = System.currentTimeMillis();
        broadcastData(seconds, information, value, MessageType.DATA);
    }

    public enum MessageType {
        ERROR, STATUS, DATA;
    }

    public static class DataObject implements Serializable {
        public long time;
        public String information;
        public Serializable value;
        public MessageType dataType;

        public DataObject() { }

        public DataObject (String information, Serializable value) {
            time = System.currentTimeMillis();
            this.information = information;
            this.value = value;
            dataType = MessageType.DATA;
        }

        public DataObject clone() {
            DataObject dobj = new DataObject();
            dobj.time = time;
            dobj.value = value;
            dobj.dataType = dataType;
            return dobj;
        }

        public String toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("time", time);
                jsonObject.put("information", information);
                jsonObject.put("value", value.toString());
                jsonObject.put("dataType", dataType.toString());
            } catch (JSONException jse) {
                return null;
            }
            return jsonObject.toString();
        }
    }

}
