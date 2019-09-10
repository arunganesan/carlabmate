package edu.umich.carlab.net;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.utils.Utilities;


/**
 * Keep internal database or store in files.
 * Periodically wake up if there is data to upload it
 * See UploadFiles code.
 */
public class PacketHandleService extends Service {
    public final String TAG = PacketHandleService.class.getName();
    ScheduledFuture<?> scheduled = null;
    final IBinder mBinder = new PacketHandleService.LocalBinder();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    Map<String, List<DataMarshal.DataObject>> allData = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        // Or an intent
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }


    /****************************************************************
     * Public functions
     ***************************************************************/
    public void outputNewInfo (String info, DataMarshal.DataObject data) {
        /**
         * Add the data to the internal database
         * Will be uploaded asynchronously
         */

        // Save it locally
        synchronized (allData) {
            if (!allData.containsKey(info))
                allData.put(info, new ArrayList<DataMarshal.DataObject>());

            allData.get(info).add(data);
        }
    }

    public Map<String, Object> checkNewInfo () {
        /**
         * If there data in the local database, then return those pairs
         * This is a non-blocking call
         */
        return null;
    }


    Runnable  uploadRunnable = new Runnable() {
        public void run() {
            // Save all data to disk
            synchronized (allData) {
                for (String info : allData.keySet())
                    saveOrUpdateData(info, allData.get(info));
                allData.clear();
            }


            // Load all data stored in the local file
            Map<String, List<DataMarshal.DataObject>> storedData = loadAllData();

            // Then for each info, make a call to upload
            for (String info : storedData.keySet()) {
                // If upload successful, delete file locally
                RequestQueue queue = Volley.newRequestQueue(context);

                // Get the list of currently uploaded receipts. Only upload ones that weren't succesfully
                // uploaded and processed

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("message", "json-ify the storedData.get(info)");
                } catch (Exception e) {
                    Log.e(TAG, "JSON error...");
                }

                JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST,
                        "http:...1234/upload?uid=...",
                        jsonObject,
                        gotListSuccess,
                        volleyError) {
                };
            }

        }
    };

    Response.Listener<JSONObject> gotListSuccess = new Response.Listener<String>() {
        @Override
        public void onResponse(String responseJSON) {
            try {
               // get the info name that was uploaded
               // and the last timestamp for that was uploaded
               // and delete that from local file
            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSON of list of uploaded receipts");
            }
        }
    };

    Response.ErrorListener volleyError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };

    public void dumpData(List<DataMarshal.DataObject> dataObjects) {
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataObjects);
            oos.close();
            fos.close();
            Log.v(TAG, "Saved data dump");
            Toast.makeText(context, "Saved file to " + saveFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write file");
        }
    }

    public List<DataMarshal.DataObject> readData(File ifile) {
        List<DataMarshal.DataObject> returnData = null;

        try {
            FileInputStream fis = new FileInputStream(ifile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            returnData = (List<DataMarshal.DataObject>)ois.readObject();
            ois.close();
            fis.close();
            Log.v(TAG, "Loaded data");
        } catch (Exception e) {
            Log.e(TAG, "Failed to write file");
        }

        return returnData;
    }

    public void scheduleUploads () {
        if (scheduled == null) {
            scheduled = scheduler.scheduleAtFixedRate(
                    uploadRunnable,
                    0,
                    1,
                    TimeUnit.MINUTES);
        }
    }

    public void unscheduleUploads () {
        if (scheduled != null) {
            scheduled.cancel(false);
        }
    }


    public class LocalBinder extends Binder {
        public PacketHandleService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PacketHandleService.this;
        }
    }
}


