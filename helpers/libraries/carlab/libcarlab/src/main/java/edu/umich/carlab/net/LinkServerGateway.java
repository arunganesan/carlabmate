package edu.umich.carlab.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.io.MultipartUtility;
import edu.umich.carlab.utils.Utilities;


/**
 * Keep internal database or store in files.
 * Periodically wake up if there is data to upload it
 */
public class LinkServerGateway extends Service {
    public final String TAG = LinkServerGateway.class.getName();
    ScheduledFuture<?> scheduled = null;
    final IBinder mBinder = new LinkServerGateway.LocalBinder();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    Map<String, List<DataMarshal.DataObject>> allData = new HashMap<>();
    SharedPreferences prefs;
    final String FILE_INFO_MAPPING = "saved file info mapping";


    final int USERID = 21;
    final String UPLOAD_URL = "http://localhost:3000/packet/upload?information=%s&person=" + USERID;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        // Or an intent
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
    public Map<String, Object> checkNewInfo () {
        // TODO When new info comes in, we need to call DataMarshal's "broadcast new data"
        // Or directly call CLService.newData()
        DataMarshal dm = new DataMarshal(null);
        dm.broadcastData("info", "value");
        return null;

    }
    
    public void addNewData (DataMarshal.DataObject data) {
        /**
         * Add the data to the internal database
         * Will be uploaded asynchronously
         */
        // Save it locally
        synchronized (allData) {
            if (!allData.containsKey(data.information))
                allData.put(
                        data.information,
                        new ArrayList<DataMarshal.DataObject>());

            allData.get(data.information).add(data);
        }
    }


    public static File GetTripsDir(Context context) {
        File tracesDir = context.getExternalFilesDir("traces");
        tracesDir.mkdirs();
        return tracesDir;
    }

    File getNewFile (String infoname) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        String filename = infoname + "-" + dateFormatter.format(Calendar.getInstance().getTime())+ ".json";
        return new File(GetTripsDir(this), filename);
    }

    Runnable  uploadRunnable = new Runnable() {
        public void run() {
            // Save all data to disk
            Map<String, List<DataMarshal.DataObject>> dataHolder;
            synchronized (allData) {
                dataHolder = allData;
                allData = new HashMap<>();
            }

            String fileInfoMappingJson = prefs.getString(FILE_INFO_MAPPING, "{}");
            JSONObject fileInfoMapping;

            try {
                fileInfoMapping = new JSONObject(fileInfoMappingJson);

                for (String info : dataHolder.keySet()) {
                    File saveFile = getNewFile(info);


                    FileOutputStream fos = new FileOutputStream(saveFile);
                    GZIPOutputStream gos = new GZIPOutputStream(fos);
                    OutputStreamWriter osw = new OutputStreamWriter(gos);
                    BufferedWriter buf = new BufferedWriter(osw);

                    for (DataMarshal.DataObject dataObject : dataHolder.get(info)) {
                        String line = dataObject.toJson();
                        if (line != null) {
                            buf.write(line, 0, line.length());
                            buf.newLine();
                        }
                    }

                    buf.flush();
                    buf.close();

                    if (!fileInfoMapping.has(info))
                        fileInfoMapping.put(info, new JSONArray());

                    JSONArray existingFiles = fileInfoMapping.getJSONArray(info);
                    existingFiles.put(saveFile.getAbsoluteFile());
                    fileInfoMapping.put(info, existingFiles);
                    prefs.edit().putString(FILE_INFO_MAPPING, fileInfoMapping.toString()).commit();
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write Packet info to file");
            } catch (JSONException je) {
                Log.e(TAG, "Couldn't load file info mapping JSON file");
            }


            // Load all data stored in the local file
            // Get the info/file mapping
            // For each file, try uploading using "file" post.
            // If succeeds, delete that from file system and from shared prefs.
            if (!Utilities.isConnectedAndWifi(LinkServerGateway.this)) return;

            try {
                fileInfoMappingJson = prefs.getString(FILE_INFO_MAPPING, "{}");
                fileInfoMapping = new JSONObject(fileInfoMappingJson);
                JSONObject remainingFileInfoMapping = new JSONObject();

                String info, filename;
                JSONArray filenames;
                File file;
                Iterator<String> keysIterator = fileInfoMapping.keys();
                int returnCode;
                List<String> failedUploads = new ArrayList<>();

                while (keysIterator.hasNext()) {
                    info = keysIterator.next();

                    filenames = fileInfoMapping.getJSONArray(info);
                    for (int i = 0; i < filenames.length(); i++) {
                        filename = filenames.getString(i);
                        file = new File(filename);
                        if (!file.exists())
                            continue;


                        URL url = new URL(String.format(UPLOAD_URL, info));
                        MultipartUtility mpu = new MultipartUtility(url);
                        mpu.addFilePart("file", new File(filename));
                        returnCode = mpu.finishCode();
                        if (returnCode == HttpURLConnection.HTTP_OK)
                            // Delete file if upload succeeds
                            file.delete();
                        else
                            // These will be uploaded again in the future
                            failedUploads.add(filename);
                    }

                    remainingFileInfoMapping.put(info, new JSONArray(failedUploads));
                }

                // Delete successful files from shared prefs and file system
                prefs.edit()
                        .putString(FILE_INFO_MAPPING, remainingFileInfoMapping.toString())
                        .commit();


            } catch (MalformedURLException mue) {
                Log.e(TAG, "Malformed URL: " + mue.getLocalizedMessage());
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write Packet info to file");
            } catch (JSONException je) {
                Log.e(TAG, "Couldn't load file info mapping JSON file");
            }
        }
    };


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
        public LinkServerGateway getService() {
            // Return this instance of LocalService so clients can call public methods
            return LinkServerGateway.this;
        }
    }
}


