package edu.umich.carlab.io;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.clog.CLog;
import edu.umich.carlab.hal.HardwareAbstractionLayer;
import edu.umich.carlab.trips.TripLog;
import edu.umich.carlab.trips.TripRecord;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * Class which accepts data and writes to a file
 */

public class CLTripWriter {
    private final String TAG = "TripWriter";
    private Context context;
    private TripRecord tripRecord;
    private String filename;
    private File saveFile;
    private BufferedWriter buf;
    private Date startTime;
    private SharedPreferences prefs;

    private Set<String> appsEncountered;
    private Set<String> sensorsSaved;


    public CLTripWriter(Context context, TripRecord tripRecord) {
        this.context = context;
        this.tripRecord = tripRecord;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        filename = dateFormatter.format(Calendar.getInstance().getTime()) + "-T" + tripRecord.getID() + ".json";
        saveFile = new File(CLTripWriter.GetTripsDir(context), filename);
        appsEncountered = new HashSet<>();
        sensorsSaved = new HashSet<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (tripRecord.getID() == null) {
            Log.e(TAG, "The trip is empty.");
            CLog.e(TAG, "The trip was empty: " + tripRecord.toString());
        } else {
            prefs.edit()
                    .putInt(filename + ":trip", tripRecord.getID())
                    .apply();
        }
    }

    public static File GetDumpsDir(Context context) {
        File tracesDir = context.getExternalFilesDir("dumps");
        tracesDir.mkdirs();
        return tracesDir;
    }

    public static File GetSpecsDir(Context context) {
        File tracesDir = context.getExternalFilesDir("specs");
        tracesDir.mkdirs();
        return tracesDir;
    }


    public static File GetResultsDir(Context context) {
        File tracesDir = context.getExternalFilesDir("results");
        tracesDir.mkdirs();
        return tracesDir;
    }

    public static File GetTripsDir(Context context) {
        File tracesDir = context.getExternalFilesDir("traces");
        tracesDir.mkdirs();
        return tracesDir;
    }

    public static File GetSurveyDir(Context context) {
        File surveyDir = context.getExternalFilesDir("survey");
        surveyDir.mkdirs();
        return surveyDir;
    }

    public static File GetAudioDir(Context context) {
        File audioDst = context.getExternalFilesDir("audio");
        audioDst.mkdirs();
        return audioDst;
    }

    public static File GetAppsDir(Context context) {
        File appsDst = context.getFilesDir();
        appsDst.mkdirs();
        return appsDst;
    }

    public void startNewTrip() {
        try {
            startTime = Calendar.getInstance().getTime();
            FileOutputStream fos = new FileOutputStream(saveFile);
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(gos);
            this.buf = new BufferedWriter(osw);
            Log.d(TAG, "Constructed the LogCSVWriter");
        } catch (Exception e) {
            Log.e(TAG, "LogCSVWriter constructor failed");
        }
    }


    /**
     * @param dataObject
     */
    public synchronized void addNewData(DataMarshal.DataObject dataObject) {
        // If data object has more than 1 sensor, we have to split it up
        if (dataObject.value.length > 1) {
            List<DataMarshal.DataObject> splitObjects = HardwareAbstractionLayer.splitDataObjects(dataObject);
            for (DataMarshal.DataObject dObject : splitObjects) {
                String line = dObject.toJson();

                if (line != null) {
                    try {
                        buf.write(line, 0, line.length());
                        buf.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            String line = dataObject.toJson();

            if (line != null) {
                try {
                    buf.write(line, 0, line.length());
                    buf.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public String stopTrip() {
        tripRecord.setEndDate(new Date());
        TripLog tripLog = TripLog.getInstance(context);
        tripLog.updateRecord(tripRecord);
        try {
            buf.flush();
            buf.close();

            Log.d(TAG, "Flushed and closed");

            Date endTime = Calendar.getInstance().getTime();
            long durationInMillis = endTime.getTime() - startTime.getTime();

            // Save this to shared prefs when we load the file
            prefs.edit()
                    .putStringSet(filename + ":sensors", sensorsSaved)
                    .putLong(filename + ":duration", durationInMillis)
                    .apply();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filename;
    }

    public TripRecord getCurrentTripRecord() {
        return tripRecord;
    }

}
